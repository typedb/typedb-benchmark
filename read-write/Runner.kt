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
package com.vaticle.typedb.benchmark.readwrite

import com.vaticle.typedb.benchmark.readwrite.common.Config
import com.vaticle.typedb.benchmark.readwrite.common.Context
import com.vaticle.typedb.benchmark.readwrite.common.ModelParams
import com.vaticle.typedb.benchmark.framework.common.params.Database
import com.vaticle.typedb.benchmark.framework.common.params.Options
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBClient
import java.lang.IllegalArgumentException

class Runner : com.vaticle.typedb.benchmark.framework.Runner<ModelParams>() {

    override fun initSimulation(options: Options, config: Config): com.vaticle.typedb.benchmark.framework.Simulation<*, *> {
        val context = Context.create(config = config, isTracing = options.tracing != null, isReporting = false)
        return when (options.database) {
            Database.TYPEDB -> StorageBenchmark(TypeDBClient.core(options.address, context.dbName), context).apply { init() }
            Database.TYPEDB_CLUSTER -> StorageBenchmark(TypeDBClient.cluster(options.address, context.dbName), context).apply { init() }
            Database.NEO4J -> throw IllegalArgumentException("Neo4j simulation is not implemented.")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Runner().run(args) { yaml -> ModelParams.of(yaml) }
        }
    }
}
