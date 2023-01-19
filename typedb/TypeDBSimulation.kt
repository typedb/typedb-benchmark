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
package com.vaticle.typedb.benchmark.typedb

import com.vaticle.typedb.benchmark.common.Util.printDuration
import com.vaticle.typedb.benchmark.common.concept.City
import com.vaticle.typedb.benchmark.common.concept.Continent
import com.vaticle.typedb.benchmark.common.concept.Country
import com.vaticle.typedb.benchmark.common.concept.Currency
import com.vaticle.typedb.benchmark.common.concept.Global
import com.vaticle.typedb.benchmark.common.concept.University
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.SeedData
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
import com.vaticle.typedb.benchmark.typedb.Labels.CITY
import com.vaticle.typedb.benchmark.typedb.Labels.CODE
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER
import com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS
import com.vaticle.typedb.benchmark.typedb.Labels.CONTINENT
import com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY
import com.vaticle.typedb.benchmark.typedb.Labels.CURRENCY
import com.vaticle.typedb.benchmark.typedb.Labels.LOCATED
import com.vaticle.typedb.benchmark.typedb.Labels.LOCATES
import com.vaticle.typedb.benchmark.typedb.Labels.LOCATION
import com.vaticle.typedb.benchmark.typedb.Labels.NAME
import com.vaticle.typedb.benchmark.typedb.Labels.UNIVERSITY
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBCitizenshipAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBCoupleFriendshipAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBFriendshipAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBGrandparenthoodAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBLineageAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBMaritalStatusAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBMarriageAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBNationalityAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBParenthoodAgent
import com.vaticle.typedb.benchmark.typedb.agent.TypeDBPersonAgent
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBSession
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction
import com.vaticle.typedb.client.api.TypeDBSession.Type.DATA
import com.vaticle.typedb.client.api.TypeDBSession.Type.SCHEMA
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE
import com.vaticle.typeql.lang.TypeQL
import com.vaticle.typeql.lang.TypeQL.insert
import com.vaticle.typeql.lang.TypeQL.match
import com.vaticle.typeql.lang.TypeQL.rel
import com.vaticle.typeql.lang.TypeQL.`var`
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

class TypeDBSimulation private constructor(client: TypeDBClient, context: Context) :
    Simulation<TypeDBClient, TypeDBSession, TypeDBTransaction>(client, context) {

    override fun initialise(geoData: SeedData) {
        val nativeClient = client.unpack()
        initDatabase(nativeClient)
        initSchema(nativeClient)
        initData(nativeClient, geoData)
    }

    private fun initDatabase(nativeClient: com.vaticle.typedb.client.api.TypeDBClient) {
        if (nativeClient.databases().contains(context.dbName)) {
            nativeClient.databases()[context.dbName].delete()
        }
        nativeClient.databases().create(context.dbName)
    }

    private fun initSchema(nativeClient: com.vaticle.typedb.client.api.TypeDBClient) {
        nativeClient.session(context.dbName, SCHEMA).use { session ->
            LOGGER.info("TypeDB initialisation of world simulation schema started ...")
            val start = Instant.now()
            val schemaQuery = Files.readString(SCHEMA_FILE.toPath())
            session.transaction(WRITE).use { tx ->
                tx.query().define(TypeQL.parseQuery(schemaQuery))
                tx.commit()
            }
            LOGGER.info(
                "TypeDB initialisation of world simulation schema ended in: {}", printDuration(start, Instant.now())
            )
        }
    }

    private fun initData(nativeClient: com.vaticle.typedb.client.api.TypeDBClient, geoData: SeedData) {
        nativeClient.session(context.dbName, DATA).use { session ->
            LOGGER.info("TypeDB initialisation of world simulation data started ...")
            val start = Instant.now()
            initContinents(session, geoData.global)
            LOGGER.info("TypeDB initialisation of world simulation data ended in: {}", printDuration(start, Instant.now()))
        }
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

    override fun createPersonAgent(client: TypeDBClient, context: Context): PersonAgent<TypeDBTransaction> {
        return TypeDBPersonAgent(client, context)
    }

    override fun createFriendshipAgent(client: TypeDBClient, context: Context): FriendshipAgent<TypeDBTransaction> {
        return TypeDBFriendshipAgent(client, context)
    }

    override fun createMarriageAgent(client: TypeDBClient, context: Context): MarriageAgent<TypeDBTransaction> {
        return TypeDBMarriageAgent(client, context)
    }

    override fun createParenthoodAgent(client: TypeDBClient, context: Context): ParenthoodAgent<TypeDBTransaction> {
        return TypeDBParenthoodAgent(client, context)
    }

    override fun createLineageAgent(client: TypeDBClient, context: Context): LineageAgent<TypeDBTransaction> {
        return TypeDBLineageAgent(client, context)
    }

    override fun createNationalityAgent(client: TypeDBClient, context: Context): NationalityAgent<TypeDBTransaction> {
        return TypeDBNationalityAgent(client, context)
    }

    override fun createCitizenshipAgent(client: TypeDBClient, context: Context): CitizenshipAgent<TypeDBTransaction> {
        return TypeDBCitizenshipAgent(client, context)
    }

    override fun createMaritalStatusAgent(client: TypeDBClient, context: Context): MaritalStatusAgent<TypeDBTransaction> {
        return TypeDBMaritalStatusAgent(client, context)
    }

    override fun createCoupleFriendshipAgent(
        client: TypeDBClient, context: Context
    ): CoupleFriendshipAgent<TypeDBTransaction> {
        return TypeDBCoupleFriendshipAgent(client, context)
    }

    override fun createGrandparenthoodAgent(
        client: TypeDBClient, context: Context
    ): GrandparenthoodAgent<TypeDBTransaction> {
        return TypeDBGrandparenthoodAgent(client, context)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val SCHEMA_FILE = Paths.get("typedb/simulation.tql").toFile()
        private const val X = "x"
        private const val Y = "y"

        fun core(address: String, context: Context): TypeDBSimulation {
            return TypeDBSimulation(TypeDBClient.core(address, context.dbName), context)
        }

        fun cluster(address: String, context: Context): TypeDBSimulation {
            return TypeDBSimulation(TypeDBClient.cluster(address, context.dbName), context)
        }
    }
}
