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

import com.vaticle.typedb.simulation.common.params.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

open class Context<out SEED_DATA, out MODEL_PARAMS>(
    val seedData: SEED_DATA, private val config: Config<MODEL_PARAMS>,
    private val _isTracing: Boolean, val isReporting: Boolean
) : AutoCloseable {
    private val iteration = AtomicInteger(1)
    val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val agentConfigs = config.agents
    val seed = config.run.randomSeed
    val dbName = config.run.databaseName
    val iterationMax = config.run.iterations
    val partitionCount = config.run.partitions

    val model = config.model

    val iterationNumber get(): Int {
        return iteration.get()
    }

    fun incrementIteration() {
        iteration.incrementAndGet()
    }

    val isTracing get(): Boolean {
        if (!_isTracing) return false
        val traceSampling = requireNotNull(config.traceSampling) { "Tracing was requested, but 'traceSampling' is not configured" }
        return traceSampling.function(iterationNumber)
    }

    override fun close() {
        executor.shutdown()
    }
}
