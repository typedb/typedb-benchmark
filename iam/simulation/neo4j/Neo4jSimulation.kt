/*
 * Copyright (C) 2022 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.vaticle.typedb.iam.simulation.neo4j

import com.vaticle.typedb.iam.simulation.agent.PersonAgent
import com.vaticle.typedb.iam.simulation.common.Util.printDuration
import com.vaticle.typedb.iam.simulation.common.concept.City
import com.vaticle.typedb.iam.simulation.common.concept.Continent
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Global
import com.vaticle.typedb.iam.simulation.common.concept.University
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.ASSERT
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.CONSTRAINT
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.CREATE
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.IS_UNIQUE
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.MATCH
import com.vaticle.typedb.iam.simulation.neo4j.Keywords.ON
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CITY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CITY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CODE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COMPANY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COMPANY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CONTAINED_IN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CONTINENT
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CONTINENT_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.COUNTRY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.CURRENCY
import com.vaticle.typedb.iam.simulation.neo4j.Literals.EMAIL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.ID
import com.vaticle.typedb.iam.simulation.neo4j.Literals.LICENCE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.LOCATED_IN
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MARRIAGE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.MARRIAGE_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.NAME
import com.vaticle.typedb.iam.simulation.neo4j.Literals.NUMBER
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PERSON_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PRODUCT
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PRODUCT_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PURCHASE
import com.vaticle.typedb.iam.simulation.neo4j.Literals.PURCHASE_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.REGION_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.Literals.UNIVERSITY_LABEL
import com.vaticle.typedb.iam.simulation.neo4j.agent.Neo4jAgentFactory
import com.vaticle.typedb.simulation.neo4j.Neo4jClient
import mu.KotlinLogging
import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import java.time.Instant

class Neo4jSimulation private constructor(client: Neo4jClient, context: Context)
    : com.vaticle.typedb.simulation.neo4j.Neo4jSimulation<Context>(client, context, Neo4jAgentFactory(client, context)) {

    override val agentPackage: String = PersonAgent::class.java.packageName

    override val name = "IAM"

    /**
     * Neo4j Community can create only uniqueness constraints, and only on nodes, not relationships. This means that it
     * does not enforce the existence of a property on those nodes. `exists()` is only available in Neo4j Enterprise.
     * https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
     *
     * @param session
     */
    override fun addKeyConstraints(session: Session) {
        val queries: List<String> = listOf(
            "$CREATE $CONSTRAINT unique_person_email $ON ($PERSON:$PERSON_LABEL) $ASSERT $PERSON.$EMAIL $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_continent_code $ON ($CONTINENT:$CONTINENT_LABEL) $ASSERT $CONTINENT.$CODE $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_country_code $ON ($COUNTRY:$COUNTRY_LABEL) $ASSERT $COUNTRY.$CODE $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_city_code $ON ($CITY:$CITY_LABEL) $ASSERT $CITY.$CODE $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_company_number $ON ($COMPANY:$COMPANY_LABEL) $ASSERT $COMPANY.$NUMBER $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_product_id $ON ($PRODUCT:$PRODUCT_LABEL) $ASSERT $PRODUCT.$ID $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_purchase_id $ON ($PURCHASE:$PURCHASE_LABEL) $ASSERT $PURCHASE.$ID $IS_UNIQUE",
            "$CREATE $CONSTRAINT unique_marriage_licence $ON ($MARRIAGE:$MARRIAGE_LABEL) $ASSERT $MARRIAGE.$LICENCE $IS_UNIQUE",
        )
        val tx = session.beginTransaction()
        queries.forEach { tx.run(Query(it)) }
        tx.commit()
    }

    override fun initData(nativeDriver: Driver) {
        LOGGER.info("Neo4j initialisation of world simulation data started ...")
        val start = Instant.now()
        initContinents(nativeDriver, context.seedData.global)
        LOGGER.info("Neo4j initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()))
    }

    private fun initContinents(nativeDriver: Driver, global: Global) {
        global.continents.parallelStream().forEach { continent: Continent ->
            nativeDriver.session().use { session ->
                val tx = session.beginTransaction()
                tx.run(Query("$CREATE (x:$CONTINENT_LABEL:$REGION_LABEL {$CODE: '${continent.code}', $NAME: '${escapeQuotes(continent.name)}'})"))
                tx.commit()
                initCountries(nativeDriver, continent)
            }
        }
    }

    private fun initCountries(nativeDriver: Driver, continent: Continent) {
        continent.countries.parallelStream().forEach { country: Country ->
            nativeDriver.session().use { session ->
                val tx = session.beginTransaction()
                val currencyProps = StringBuilder()
                if (country.currencies.isNotEmpty()) {
                    currencyProps.append(", ")
                    for (i in country.currencies.indices) {
                        val currency = country.currencies[i]
                        currencyProps.append(CURRENCY).append(i + 1).append(": '").append(currency.code).append("'")
                        if (i + 1 < country.currencies.size) currencyProps.append(", ")
                    }
                }
                val query = Query(
                    "$MATCH (c:$CONTINENT_LABEL {$CODE: '${continent.code}'}) " +
                            "$CREATE (x:$COUNTRY_LABEL:$REGION_LABEL {$CODE: '${country.code}', $NAME: '${escapeQuotes(country.name)}'$currencyProps})-[:$CONTAINED_IN]->(c)"
                )
                tx.run(query)
                tx.commit()
                initCities(session, country)
                initUniversities(session, country)
            }
        }
    }

    private fun initCities(session: Session, country: Country) {
        val tx = session.beginTransaction()
        country.cities.forEach { city: City ->
            val query = Query(
                "$MATCH (c:$COUNTRY_LABEL {$CODE: '${country.code}'}) " +
                        "$CREATE (x:$CITY_LABEL:$REGION_LABEL {$CODE: '${city.code}', $NAME: '${escapeQuotes(city.name)}'})-[:$CONTAINED_IN]->(c)",
            )
            tx.run(query)
        }
        tx.commit()
    }

    private fun initUniversities(session: Session, country: Country) {
        val tx = session.beginTransaction()
        country.universities.forEach { university: University ->
            val query = Query(
                "$MATCH (c:$COUNTRY_LABEL {$CODE: '${country.code}'}) $CREATE (x:$UNIVERSITY_LABEL {$NAME: '${escapeQuotes(university.name)}'})-[:$LOCATED_IN]->(c)",
            )
            tx.run(query)
        }
        tx.commit()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun create(hostUri: String, context: Context): Neo4jSimulation {
            return Neo4jSimulation(Neo4jClient(hostUri), context).apply { init() }
        }
    }
}
