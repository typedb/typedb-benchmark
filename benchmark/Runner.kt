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
package com.vaticle.typedb.benchmarks.storage


import com.vaticle.typedb.benchmarks.storage.common.Config
import com.vaticle.typedb.benchmarks.storage.common.Context
import com.vaticle.typedb.benchmarks.storage.common.ModelParams
import com.vaticle.typedb.simulation.common.params.Database
import com.vaticle.typedb.simulation.common.params.Options
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import simulation.StorageBenchmark
import java.lang.IllegalArgumentException

public class Runner : com.vaticle.typedb.simulation.Runner<ModelParams>() {

    override fun initSimulation(options: Options, config: Config): com.vaticle.typedb.simulation.Simulation<*, *> {
        val context = Context.create(config = config, isTracing = options.tracing != null, isReporting = false)
        return when (options.database) {
            Database.TYPEDB -> StorageBenchmark(TypeDBClient.core(options.address, context.dbName), context).apply { init() }
            Database.TYPEDB_CLUSTER -> throw IllegalArgumentException("TypeDB simulation is not currently implemented.")
            Database.NEO4J -> throw IllegalArgumentException("Neo4j simulation is not currently implemented.")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            Runner().run(args) { yaml -> ModelParams.of(yaml) }
        }
    }
}
