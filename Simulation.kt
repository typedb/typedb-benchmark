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

import com.vaticle.typedb.simulation.common.Util.printDuration
import com.vaticle.typedb.simulation.common.driver.Client
import com.vaticle.typedb.simulation.common.seed.RandomSource
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

abstract class Simulation<CLIENT: Client<*>, out CONTEXT: Context<*, *>>(
    protected val client: CLIENT, protected val context: CONTEXT, protected val agentFactory: Agent.Factory
) : AutoCloseable {
    private val randomSource = RandomSource(context.seed)
    private val agents: List<Agent<*, *, *>> = initAgents()
    private val agentReports = ConcurrentHashMap<String, Map<String, List<Agent.Report>>>()
    private val _registeredAgents = mutableListOf<Class<out Agent<*, *, *>>>()
    val registeredAgents: List<Class<out Agent<*, *, *>>> get() = _registeredAgents

    protected abstract val name: String

    init {
        initialise()
    }

    protected abstract fun initialise()

    private fun initAgents(): List<Agent<*, *, *>> {
        return context.agentConfigs.filter { it.isEnabled }.map { agentConfig ->
            val className = "${Agent::class.java.packageName}.${agentConfig.name}"
            val agentClass = Class.forName(className) as Class<out Agent<*, *, *>>
            val agent = agentFactory[agentClass]?.let { it() } ?: throw RuntimeException("${agentConfig.name} is not registered as an agent")
            agent.apply { tracingEnabled = agentConfig.trace }.also { _registeredAgents += agentClass }
        }
    }

    fun getReport(agentClass: Class<out Agent<*, *, *>>): Map<String, List<Agent.Report>> {
        return agentReports[agentClass.simpleName] ?: throw RuntimeException("Agent ${agentClass.simpleName} has no report")
    }

    fun run() {
        val start = Instant.now()
        while (context.iterationNumber <= context.iterationMax) {
            val iter = context.iterationNumber
            val iterStart = Instant.now()
            iterate()
            LOGGER.info("Iteration {}: {}", iter, printDuration(iterStart, Instant.now()))
            LOGGER.info("-------------------------")
        }
        LOGGER.info("$name simulation run duration: " + printDuration(start, Instant.now()))
        LOGGER.info(client.printStatistics())
    }

    fun iterate() {
        agentReports.clear()
        agents.forEach { agent ->
            val start = Instant.now()
            val reports = agent.iterate(randomSource.nextSource())
            LOGGER.info("{} took: {}", agent.javaClass.simpleName, printDuration(start, Instant.now()))
            agentReports[agent.javaClass.superclass.simpleName] = reports
        }
        context.incrementIteration()
    }

    override fun close() {
        client.close()
        context.close()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
