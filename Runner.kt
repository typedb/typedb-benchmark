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
package com.vaticle.typedb.simulation

import com.vaticle.factory.tracing.client.FactoryTracing
import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic
import com.vaticle.typedb.common.yaml.YAML
import com.vaticle.typedb.simulation.common.params.Config
import com.vaticle.typedb.simulation.common.params.Options
import mu.KotlinLogging
import kotlin.system.exitProcess

abstract class Runner<MODEL> {

    fun run(args: Array<String>, modelConfigParseFn: (YAML.Map) -> MODEL) {
        try {
            val options = Options.parseCLIOptions(args) ?: exitProcess(0)
            initTracing(options = options.tracing, analysisName = options.database.fullName).use {
                val config = Config.of(options.configFile, modelConfigParseFn)
                initSimulation(options, config).use { simulation -> simulation.run() }
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            exitProcess(1)
        }
    }

    protected abstract fun initSimulation(options: Options, config: Config<MODEL>): Simulation<*, *>

    private fun initTracing(options: Options.FactoryTracing?, analysisName: String): FactoryTracing {
        if (options == null) return FactoryTracing.createNoOp()
        val tracing: FactoryTracing = when (val credentials = options.credentials) {
            null -> FactoryTracing.create(options.factoryURI).withLogging()
            else -> FactoryTracing.create(options.factoryURI, credentials.username, credentials.token).withLogging()
        }
        FactoryTracingThreadStatic.setGlobalTracingClient(tracing)
        val taggedAnalysisName = when (val tags = options.tags) {
            null -> analysisName
            else -> "$analysisName [ ${tags.joinToString(", ")} ]"
        }
        FactoryTracingThreadStatic.openGlobalAnalysis(options.org, options.repo, options.commit, taggedAnalysisName)
        return tracing
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
