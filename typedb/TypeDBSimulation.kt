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
package com.vaticle.typedb.simulation.typedb

import com.vaticle.typedb.simulation.common.Util.printDuration
import com.vaticle.typedb.simulation.Context
import com.vaticle.typedb.simulation.Simulation
import com.vaticle.typedb.client.api.TypeDBSession.Type.DATA
import com.vaticle.typedb.client.api.TypeDBSession.Type.SCHEMA
import com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE
import com.vaticle.typedb.simulation.Agent
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typeql.lang.TypeQL
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.time.Instant

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
        val whitespaceRegex = Regex(WHITESPACE_REGEX, RegexOption.IGNORE_CASE)
        nativeClient.session(context.dbName, SCHEMA).use { session ->
            LOGGER.info("TypeDB initialisation of $name simulation schema started ...")
            val start = Instant.now()
            session.transaction(WRITE).use { tx ->
                schemaFiles.forEach { schemaFile ->
                    val schemaQueries = splitSchemaQueries(Files.readString(schemaFile.toPath()))
                    schemaQueries.forEach { schemaQuery ->
                        when (val firstWord = schemaQuery.trim().split(whitespaceRegex).first().lowercase()) {
                            "define" -> tx.query().define(TypeQL.parseQuery(schemaQuery))
                            "undefine" -> tx.query().undefine(TypeQL.parseQuery(schemaQuery))
                            else -> throw IllegalArgumentException("Schema query must start with 'define' or 'undefine', not '$firstWord'.")
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

    private fun splitSchemaQueries(queryString: String): List<String> {
        val commentRegex = Regex(COMMENT_REGEX, RegexOption.IGNORE_CASE)
        val whitespaceRegex = Regex(WHITESPACE_REGEX, RegexOption.IGNORE_CASE)
        val firstWord = queryString
            .replace(commentRegex, "")
            .trim()
            .split(whitespaceRegex)
            .first()
            .lowercase()
        val secondWord: String
        val firstRegex: Regex
        val secondRegex: Regex
        when (firstWord) {
            "define" -> {
                secondWord = "undefine"
                firstRegex = Regex(DEFINE_REGEX, RegexOption.IGNORE_CASE)
                secondRegex = Regex(UNDEFINE_REGEX, RegexOption.IGNORE_CASE)
            }
            "undefine" -> {
                secondWord = "define"
                firstRegex = Regex(UNDEFINE_REGEX, RegexOption.IGNORE_CASE)
                secondRegex = Regex(DEFINE_REGEX, RegexOption.IGNORE_CASE)
            }
            else -> throw IllegalArgumentException("Schema query file must start with 'define' or 'undefine', not '$firstWord'.")
        }
        return queryString
            .replace(commentRegex, "")
            .split(firstRegex)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { "$firstWord $it" }
            .map { queryBlock ->
                queryBlock.split(secondRegex)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map {
                        when (it.trim().split(whitespaceRegex).first().lowercase()) {
                            firstWord -> it
                            else -> "$secondWord $it"
                        }
                    }
            }.flatten().toList()
    }

    protected abstract fun initData(nativeSession: com.vaticle.typedb.client.api.TypeDBSession, randomSource: RandomSource)

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val DEFINE_REGEX = "(?<![\$\"]|un)define"
        private const val UNDEFINE_REGEX = "(?<![\$\"])undefine"
        private const val COMMENT_REGEX = "#.*"
        private const val WHITESPACE_REGEX = "\\s+"
    }
}
