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
package com.vaticle.typedb.benchmark.framework.typedb

import com.vaticle.typedb.benchmark.framework.common.Util.printDuration
import com.vaticle.typedb.benchmark.framework.Context
import com.vaticle.typedb.benchmark.framework.Simulation
import com.vaticle.typedb.client.api.TypeDBSession.Type.DATA
import com.vaticle.typedb.client.api.TypeDBSession.Type.SCHEMA
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE
import com.vaticle.typedb.benchmark.framework.Agent
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typeql.lang.TypeQL
import com.vaticle.typeql.lang.query.TypeQLQuery
import com.vaticle.typeql.lang.query.TypeQLDefine
import com.vaticle.typeql.lang.query.TypeQLUndefine
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.time.Instant
import kotlin.streams.toList

abstract class TypeDBSimulation<out CONTEXT: Context<*, *>> protected constructor(
    client: TypeDBClient, context: CONTEXT, agentFactory: Agent.Factory
): Simulation<TypeDBClient, CONTEXT>(client, context, agentFactory) {

    abstract val schemaFiles: List<File>

    override fun init(randomSource: RandomSource) {
        val nativeClient = client.unpack()
        initDatabase(nativeClient)
        initSchema(nativeClient)
        nativeClient.session(context.dbName, DATA).use { nativeSession ->
            LOGGER.info("TypeDB initialisation of $name simulation data started ...")
            val start = Instant.now()
            initData(nativeSession, randomSource)
            LOGGER.info("TypeDB initialisation of $name simulation data ended in: {}", printDuration(start, Instant.now()))
        }
    }

    private fun initDatabase(nativeClient: com.vaticle.typedb.client.api.TypeDBClient) {
        if (nativeClient.databases().contains(context.dbName)) {
            nativeClient.databases()[context.dbName].delete()
        }
        nativeClient.databases().create(context.dbName)
    }

    private fun initSchema(nativeClient: com.vaticle.typedb.client.api.TypeDBClient) {
        if (schemaFiles.isEmpty()) throw IllegalStateException("No schema files provided for simulation.")
        nativeClient.session(context.dbName, SCHEMA).use { session ->
            LOGGER.info("TypeDB initialisation of $name simulation schema started ...")
            val start = Instant.now()
            session.transaction(WRITE).use { tx ->
                schemaFiles.forEach { schemaFile ->
                    val schemaQueries = TypeQL.parseQueries<TypeQLQuery>(Files.readString(schemaFile.toPath())).toList()
                    schemaQueries.forEach { schemaQuery ->
                        when (schemaQuery) {
                            is TypeQLDefine -> tx.query().define(schemaQuery)
                            is TypeQLUndefine -> tx.query().undefine(schemaQuery)
                            else -> throw IllegalArgumentException("Schema file contains query types other than 'define' and 'undefine'.")
                        }
                    }
                }
                tx.commit()
            }
            LOGGER.info(
                "TypeDB initialisation of $name simulation schema ended in: {}", printDuration(start, Instant.now())
            )
        }
    }

    protected abstract fun initData(nativeSession: com.vaticle.typedb.client.api.TypeDBSession, randomSource: RandomSource)

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
