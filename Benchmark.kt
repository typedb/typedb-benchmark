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
package com.vaticle.typedb.benchmark

import com.vaticle.factory.tracing.client.FactoryTracing
import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic
import com.vaticle.typedb.benchmark.common.params.Config
import com.vaticle.typedb.benchmark.common.params.Context.Companion.create
import com.vaticle.typedb.benchmark.common.params.DatabaseType.NEO4J
import com.vaticle.typedb.benchmark.common.params.DatabaseType.TYPEDB
import com.vaticle.typedb.benchmark.common.params.DatabaseType.TYPEDB_CLUSTER
import com.vaticle.typedb.benchmark.common.params.Options
import com.vaticle.typedb.benchmark.common.params.Options.Companion.parseCLIOptions
import com.vaticle.typedb.benchmark.neo4j.Neo4jSimulation.Companion.create
import com.vaticle.typedb.benchmark.simulation.Simulation
import com.vaticle.typedb.benchmark.typedb.TypeDBSimulation.Companion.cluster
import com.vaticle.typedb.benchmark.typedb.TypeDBSimulation.Companion.core
import mu.KotlinLogging
import kotlin.system.exitProcess

object Benchmark {
    private val LOGGER = KotlinLogging.logger {}
    @JvmStatic
    fun main(args: Array<String>) {
        LOGGER.info("Welcome to the Benchmark!")
        try {
            val options = parseCLIOptions(args) ?: exitProcess(0)
            initTracing(options = options.tracing, analysisName = options.database.fullname).use {
                val config = Config.of(options.configFile)
                initSimulation(options, config).use { simulation -> simulation.run() }
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            exitProcess(1)
        }
    }

    private fun initSimulation(options: Options, config: Config): Simulation<*, *, *> {
        val context = create(config = config, isTracing = options.tracing != null, isReporting = false)
        return when (options.database) {
            TYPEDB -> core(options.address, context)
            TYPEDB_CLUSTER -> cluster(options.address, context)
            NEO4J -> create(options.address, context)
        }
    }

    private fun initTracing(options: Options.FactoryTracing?, analysisName: String): FactoryTracing {
        val tracing: FactoryTracing = when {
            options == null -> return FactoryTracing.createNoOp()
            options.credentials == null -> FactoryTracing.create(options.factoryURI).withLogging()
            else -> FactoryTracing.create(options.factoryURI, options.credentials!!.username, options.credentials!!.token)
                .withLogging()
        }
        FactoryTracingThreadStatic.setGlobalTracingClient(tracing)
        val taggedAnalysisName = when (options.tags) {
            null -> analysisName
            else -> "$analysisName [ ${options.tags!!.joinToString(", ")} ]"
        }
        FactoryTracingThreadStatic.openGlobalAnalysis(options.org, options.repo, options.commit, taggedAnalysisName)
        return tracing
    }
}
