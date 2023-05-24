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
package simulation

import com.vaticle.typedb.benchmarks.storage.AgentFactory
import com.vaticle.typedb.benchmarks.storage.PersonAgent
import com.vaticle.typedb.client.api.TypeDBOptions
import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.benchmarks.storage.common.Context
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.typedb.TypeDBClient
import mu.KotlinLogging
import java.nio.file.Paths

class StorageBenchmark internal constructor(client: TypeDBClient, context: Context):
    com.vaticle.typedb.simulation.typedb.TypeDBSimulation<Context>(client, context, AgentFactory(client, context)) {

    private val LOGGER = KotlinLogging.logger {}
    override val agentPackage = PersonAgent::class.java.packageName
    override val name = "StorageBenchmark"

    override val schemaFiles = listOf(Paths.get("schema.tql").toFile())
    private val options = TypeDBOptions.core().infer(true)

    override fun initData(nativeSession: TypeDBSession, randomSource: RandomSource) {
        LOGGER.info("TypeDB initialisation of world simulation data started ...")
    }

}
