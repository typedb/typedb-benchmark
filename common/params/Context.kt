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
package com.vaticle.typedb.benchmark.common.params

import com.vaticle.typedb.benchmark.common.seed.SeedData
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Context private constructor(
    val seedData: SeedData,
    private val config: Config,
    private val _isTracing: Boolean,
    val isReporting: Boolean
) : AutoCloseable {
    private val iteration = AtomicInteger(1)
    val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    val agentConfigs get(): List<Config.Agent> {
        return requireNotNull(config.agents)
    }

    val seed get(): Long {
        return config.runParams.randomSeed.toLong()
    }

    val scaleFactor get(): Int {
        return config.runParams.scaleFactor
    }

    val databaseName get(): String {
        return config.runParams.databaseName
    }

    val iterationMax get(): Int {
        return config.runParams.iterations
    }

    val ageOfFriendship get(): Int {
        return config.modelParams.ageOfFriendship
    }

    val ageOfAdulthood get(): Int {
        return config.modelParams.ageOfAdulthood
    }

    val yearsBeforeParenthood get(): Int {
        return config.modelParams.yearsBeforeParenthood
    }

    val iterationNumber get(): Int {
        return iteration.get()
    }

    fun incrementIteration() {
        iteration.incrementAndGet()
    }

    fun today(): LocalDateTime {
        return startDay().plusYears((iteration.get() - 1).toLong())
    }

    fun startDay(): LocalDateTime {
        return LocalDateTime.of(LocalDate.ofYearDay(2000, 1), LocalTime.of(0, 0, 0))
    }

    val isTracing get(): Boolean {
        return _isTracing && config.traceSampling!!.samplingFunction().apply(iterationNumber)!!
    }

    override fun close() {
        executor.shutdown()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Context::class.java)

        fun create(config: Config, isTracing: Boolean, isReporting: Boolean): Context {
            val seedData = SeedData.initialise()
            LOG.info("Total number of continents in seed: {}", seedData.continents.size)
            LOG.info("Total number of countries in seed: {}", seedData.countries.size)
            LOG.info("Total number of cities in seed: {}", seedData.cities.size)
            LOG.info("Total number of universities in seed: {}", seedData.universities.size)
            return Context(seedData, config, isTracing, isReporting)
        }
    }
}
