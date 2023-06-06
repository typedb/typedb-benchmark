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

import com.vaticle.typedb.client.api.TypeDBOptions
import com.vaticle.typedb.client.api.TypeDBSession
import com.vaticle.typedb.benchmark.readwrite.common.Context
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import com.vaticle.typedb.benchmark.framework.typedb.TypeDBClient
import mu.KotlinLogging
import java.nio.file.Paths

class StorageBenchmark internal constructor(client: TypeDBClient, context: Context) :
    com.vaticle.typedb.benchmark.framework.typedb.TypeDBSimulation<Context>(client, context, AgentFactory(client, context)) {

    private val LOGGER = KotlinLogging.logger {}
    override val agentPackage = PersonAgent::class.java.packageName
    override val name = "StorageBenchmark"

    // TODO: Update this filepath
    override val schemaFiles = listOf(Paths.get("read-write/schema.tql").toFile())
    private val options = TypeDBOptions.core().infer(true)

    override fun initData(nativeSession: TypeDBSession, randomSource: RandomSource) {
        LOGGER.info("Nothing to initialise")
    }
}
