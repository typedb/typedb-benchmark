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
package com.vaticle.typedb.iam.simulation.typedb

import com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE
import com.vaticle.typedb.iam.simulation.agent.AgentFactory
import com.vaticle.typedb.iam.simulation.agent.PersonAgent
import com.vaticle.typedb.iam.simulation.common.Context
import com.vaticle.typedb.iam.simulation.common.Util.printDuration
import com.vaticle.typedb.iam.simulation.common.concept.City
import com.vaticle.typedb.iam.simulation.common.concept.Continent
import com.vaticle.typedb.iam.simulation.common.concept.Country
import com.vaticle.typedb.iam.simulation.common.concept.Currency
import com.vaticle.typedb.iam.simulation.common.concept.Global
import com.vaticle.typedb.iam.simulation.common.concept.University
import com.vaticle.typedb.iam.simulation.typedb.Labels.CITY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CODE
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINED
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINER
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTAINS
import com.vaticle.typedb.iam.simulation.typedb.Labels.CONTINENT
import com.vaticle.typedb.iam.simulation.typedb.Labels.COUNTRY
import com.vaticle.typedb.iam.simulation.typedb.Labels.CURRENCY
import com.vaticle.typedb.iam.simulation.typedb.Labels.LOCATED
import com.vaticle.typedb.iam.simulation.typedb.Labels.LOCATES
import com.vaticle.typedb.iam.simulation.typedb.Labels.LOCATION
import com.vaticle.typedb.iam.simulation.typedb.Labels.NAME
import com.vaticle.typedb.iam.simulation.typedb.Labels.UNIVERSITY
import com.vaticle.typedb.iam.simulation.typedb.agent.TypeDBAgentFactory
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import com.vaticle.typeql.lang.TypeQL.insert
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import mu.KotlinLogging
import java.io.File
import java.nio.file.Paths
import java.time.Instant

class TypeDBSimulation private constructor(
    client: TypeDBClient, context: Context
) : com.vaticle.typedb.simulation.typedb.TypeDBSimulation<Context>(client, context, TypeDBAgentFactory(client, context)) {

    override val agentPackage: String = PersonAgent::class.java.packageName

    override val name = "IAM"

    override val schemaFile: File = Paths.get("iam-schema.tql").toFile()

    override fun initData(nativeSession: com.vaticle.typedb.client.api.TypeDBSession) {
        LOGGER.info("TypeDB initialisation of world simulation data started ...")
        val start = Instant.now()
        initContinents(nativeSession, context.seedData.global)
        LOGGER.info("TypeDB initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()))
    }

    private fun initContinents(session: com.vaticle.typedb.client.api.TypeDBSession, global: Global) {
        global.continents.parallelStream().forEach { continent: Continent ->
            session.transaction(WRITE).use { tx ->
                tx.query().insert(insert(`var`().isa(CONTINENT).has(CODE, continent.code).has(NAME, continent.name)))
                tx.commit()
            }
            initCountries(session, continent)
        }
    }

    private fun initCountries(session: com.vaticle.typedb.client.api.TypeDBSession, continent: Continent) {
        continent.countries.parallelStream().forEach { country: Country ->
            session.transaction(WRITE).use { tx ->
                val countryVar = `var`(Y).isa(COUNTRY).has(CODE, country.code).has(NAME, country.name)
                country.currencies.forEach { currency: Currency -> countryVar.has(CURRENCY, currency.code) }
                tx.query().insert(
                    match(
                        `var`(X).isa(CONTINENT).has(CODE, continent.code)
                    ).insert(
                        countryVar, rel(CONTAINER, X).rel(CONTAINED, Y).isa(CONTAINS)
                    )
                )
                // TODO: Currency should be an entity we relate to by relation
                tx.commit()
            }
            initCities(session, country)
            initUniversities(session, country)
        }
    }

    private fun initCities(session: com.vaticle.typedb.client.api.TypeDBSession, country: Country) {
        session.transaction(WRITE).use { tx ->
            country.cities.forEach { city: City ->
                tx.query().insert(
                    match(
                        `var`(X).isa(COUNTRY).has(CODE, country.code)
                    ).insert(
                        `var`(Y).isa(CITY).has(CODE, city.code).has(NAME, city.name),
                        rel(CONTAINER, X).rel(CONTAINED, Y).isa(CONTAINS)
                    )
                )
            }
            tx.commit()
        }
    }

    private fun initUniversities(session: com.vaticle.typedb.client.api.TypeDBSession, country: Country) {
        session.transaction(WRITE).use { tx ->
            country.universities.forEach { university: University ->
                tx.query().insert(
                    match(
                        `var`(X).isa(COUNTRY).has(CODE, country.code)
                    ).insert(
                        `var`(Y).isa(UNIVERSITY).has(NAME, university.name),
                        rel(LOCATION, X).rel(LOCATED, Y).isa(LOCATES)
                    )
                )
            }
            tx.commit()
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val X = "x"
        private const val Y = "y"

        fun core(address: String, context: Context): TypeDBSimulation {
            return TypeDBSimulation(TypeDBClient.core(address, context.dbName), context).apply { init() }
        }

        fun cluster(address: String, context: Context): TypeDBSimulation {
            return TypeDBSimulation(TypeDBClient.cluster(address, context.dbName), context).apply { init() }
        }
    }
}
