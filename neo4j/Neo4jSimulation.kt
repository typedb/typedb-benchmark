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
package com.vaticle.typedb.benchmark.neo4j

import com.vaticle.typedb.benchmark.common.Util.printDuration
import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Continent
import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Global
import com.vaticle.typedb.benchmark.common.concept.University
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.SeedData
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jFriendshipAgent
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jMarriageAgent
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jParenthoodAgent
import com.vaticle.typedb.benchmark.neo4j.agent.Neo4jPersonAgent
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jSession
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction
import com.vaticle.typedb.benchmark.simulation.Simulation
import com.vaticle.typedb.benchmark.simulation.agent.CitizenshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.GrandparenthoodAgent
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent
import com.vaticle.typedb.benchmark.simulation.agent.MaritalStatusAgent
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent
import mu.KotlinLogging
import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.function.Consumer

class Neo4jSimulation private constructor(client: Neo4jClient, context: Context) :
    Simulation<Neo4jClient, Neo4jSession, Neo4jTransaction>(client, context) {
    override fun initialise(geoData: SeedData) {
        val nativeDriver = client.unpack()
        initDatabase(nativeDriver)
        initData(nativeDriver, geoData)
    }

    private fun initDatabase(nativeDriver: Driver?) {
        nativeDriver!!.session().use { session ->
            addKeyConstraints(session)
            cleanDatabase(session)
        }
    }

    /**
     * Neo4j Community can create only uniqueness constraints, and only on nodes, not relationships. This means that it
     * does not enforce the existence of a property on those nodes. `exists()` is only available in Neo4j Enterprise.
     * https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
     *
     * @param session
     */
    private fun addKeyConstraints(session: Session) {
        val queries: List<String> = listOf(
            "CREATE CONSTRAINT unique_person_email ON (person:Person) ASSERT person.email IS UNIQUE",
            "CREATE CONSTRAINT unique_continent_code ON (continent:Continent) ASSERT continent.code IS UNIQUE",
            "CREATE CONSTRAINT unique_country_code ON (country:Country) ASSERT country.code IS UNIQUE",
            "CREATE CONSTRAINT unique_city_code ON (city:City) ASSERT city.code IS UNIQUE",
            "CREATE CONSTRAINT unique_company_number ON (company:Company) ASSERT company.number IS UNIQUE",
            "CREATE CONSTRAINT unique_product_id ON (product:Product) ASSERT product.id IS UNIQUE",
            "CREATE CONSTRAINT unique_purchase_id ON (purchase:Purchase) ASSERT purchase.id IS UNIQUE",
            "CREATE CONSTRAINT unique_marriage_licence ON (marriage:Marriage) ASSERT marriage.licence IS UNIQUE",
        )
        val tx = session.beginTransaction()
        queries.forEach { tx.run(Query(it)) }
        tx.commit()
    }

    private fun cleanDatabase(session: Session) {
        val tx = session.beginTransaction()
        tx.run(Query("MATCH (n) DETACH DELETE n"))
        tx.commit()
    }

    private fun initData(nativeDriver: Driver, geoData: SeedData) {
        LOGGER.info("Neo4j initialisation of world simulation data started ...")
        val start = Instant.now()
        initContinents(nativeDriver, geoData.global)
        LOGGER.info("Neo4j initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()))
    }

    private fun initContinents(nativeDriver: Driver, global: Global) {
        global.continents.parallelStream().forEach { continent: Continent ->
            nativeDriver.session().use { session ->
                val tx = session.beginTransaction()
                tx.run(Query("CREATE (x:Continent:Region {code: '${continent.code}', name: '${escapeQuotes(continent.name)}'})"))
                tx.commit()
                initCountries(nativeDriver, continent)
            }
        }
    }

    private fun initCountries(nativeDriver: Driver, continent: Continent) {
        continent.countries().parallelStream().forEach { country: Country ->
            nativeDriver.session().use { session ->
                val tx = session.beginTransaction()
                val currencyProps = StringBuilder()
                if (country.currencies.isNotEmpty()) {
                    currencyProps.append(", ")
                    for (i in country.currencies.indices) {
                        val currency = country.currencies[i]
                        currencyProps.append("currency").append(i + 1).append(": '").append(currency.code).append("'")
                        if (i + 1 < country.currencies.size) currencyProps.append(", ")
                    }
                }
                val query = Query(
                    "MATCH (c:Continent {code: '${continent.code}'}) " +
                            "CREATE (x:Country:Region {code: '${country.code}', name: '${escapeQuotes(country.name!!)}'$currencyProps})-[:CONTAINED_IN]->(c)"
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
                "MATCH (c:Country {code: '${country.code}'}) " +
                        "CREATE (x:City:Region {code: '${city.code}', name: '${escapeQuotes(city.name!!)}'})-[:CONTAINED_IN]->(c)",
            )
            tx.run(query)
        }
        tx.commit()
    }

    private fun initUniversities(session: Session, country: Country) {
        val tx = session.beginTransaction()
        country.universities.forEach { university: University ->
            val query = Query(
                "MATCH (c:Country {code: '${country.code}'}) CREATE (x:University {name: '${escapeQuotes(university.name)}'})-[:LOCATED_IN]->(c)",
            )
            tx.run(query)
        }
        tx.commit()
    }

    private fun escapeQuotes(string: String): String {
        return string.replace("'", "\\'")
    }

    override fun createPersonAgent(client: Neo4jClient, context: Context): PersonAgent<Neo4jTransaction> {
        return Neo4jPersonAgent(client, context)
    }

    override fun createFriendshipAgent(client: Neo4jClient, context: Context): FriendshipAgent<Neo4jTransaction> {
        return Neo4jFriendshipAgent(client, context)
    }

    override fun createMarriageAgent(client: Neo4jClient, context: Context): MarriageAgent<Neo4jTransaction> {
        return Neo4jMarriageAgent(client, context)
    }

    override fun createParenthoodAgent(client: Neo4jClient, context: Context): ParenthoodAgent<Neo4jTransaction> {
        return Neo4jParenthoodAgent(client, context)
    }

    override fun createLineageAgent(client: Neo4jClient, context: Context): LineageAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("LineageAgent requires reasoning, which is not supported by Neo4j")
    }

    override fun createNationalityAgent(client: Neo4jClient, context: Context): NationalityAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("NationalityAgent requires reasoning, which is not supported by Neo4j")
    }

    override fun createCitizenshipAgent(client: Neo4jClient, context: Context): CitizenshipAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("CitizenshipAgent requires reasoning, which is not supported by Neo4j")
    }

    override fun createMaritalStatusAgent(client: Neo4jClient, context: Context): MaritalStatusAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("MaritalStatusAgent requires reasoning, which is not supported by Neo4j")
    }

    override fun createCoupleFriendshipAgent(client: Neo4jClient, context: Context): CoupleFriendshipAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("CoupleFriendshipAgent requires reasoning, which is not supported by Neo4j")
    }

    override fun createGrandparenthoodAgent(client: Neo4jClient, context: Context): GrandparenthoodAgent<Neo4jTransaction> {
        throw UnsupportedOperationException("GrandparenthoodAgent requires reasoning, which is not supported by Neo4j")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun create(hostUri: String, context: Context): Neo4jSimulation {
            return Neo4jSimulation(Neo4jClient(hostUri), context)
        }
    }
}
