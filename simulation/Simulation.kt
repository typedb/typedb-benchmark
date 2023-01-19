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
package com.vaticle.typedb.benchmark.simulation

import com.vaticle.typedb.benchmark.common.Util.printDuration
import com.vaticle.typedb.benchmark.common.params.Context
import com.vaticle.typedb.benchmark.common.seed.RandomSource
import com.vaticle.typedb.benchmark.common.seed.SeedData
import com.vaticle.typedb.benchmark.simulation.agent.Agent
import com.vaticle.typedb.benchmark.simulation.agent.CitizenshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent
import com.vaticle.typedb.benchmark.simulation.agent.GrandparenthoodAgent
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent
import com.vaticle.typedb.benchmark.simulation.agent.MaritalStatusAgent
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent
import com.vaticle.typedb.benchmark.simulation.driver.Client
import com.vaticle.typedb.benchmark.simulation.driver.Session
import com.vaticle.typedb.benchmark.simulation.driver.Transaction
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

abstract class Simulation<CLIENT: Client<SESSION>, SESSION: Session<TX>, TX: Transaction>(
    protected val client: CLIENT, protected val context: Context
) : AutoCloseable {
    private val randomSource = RandomSource(context.seed)
    private val agents: List<Agent<*, TX>> = initAgents()
    private val agentReports: MutableMap<String, Map<String, List<Agent.Report>>> = ConcurrentHashMap()

    init {
        initialise(context.seedData)
    }

    protected abstract fun initialise(geoData: SeedData)

    private fun initAgents(): List<Agent<*, TX>> {
        val agentBuilders = initAgentBuilders()
        val agents = mutableListOf<Agent<*, TX>>()
        for (agentConfig in context.agentConfigs) {
            if (agentConfig.isEnabled) {
                val className = "$AGENT_PACKAGE.${agentConfig.name}"
                val agentClass = Class.forName(className) as Class<out Agent<*, *>>
                val agentBuilder = agentBuilders[agentClass]
                    ?: throw RuntimeException("${agentConfig.name} is not registered as an agent")
                agents.add(agentBuilder.get().apply { tracingEnabled = agentConfig.trace })
                REGISTERED_AGENTS.add(agentClass)
            }
        }
        return agents
    }

    private fun initAgentBuilders(): Map<Class<out Agent<*, *>>, Supplier<Agent<*, TX>>> {
        return object : HashMap<Class<out Agent<*, *>>, Supplier<Agent<*, TX>>>() {
            init {
                put(PersonAgent::class.java) { createPersonAgent(client, context) }
                put(FriendshipAgent::class.java) { createFriendshipAgent(client, context) }
                put(MarriageAgent::class.java) { createMarriageAgent(client, context) }
                put(ParenthoodAgent::class.java) { createParenthoodAgent(client, context) }
                put(MaritalStatusAgent::class.java) { createMaritalStatusAgent(client, context) }
                put(GrandparenthoodAgent::class.java) { createGrandparenthoodAgent(client, context) }
                put(LineageAgent::class.java) { createLineageAgent(client, context) }
                put(NationalityAgent::class.java) { createNationalityAgent(client, context) }
                put(CitizenshipAgent::class.java) { createCitizenshipAgent(client, context) }
                put(CoupleFriendshipAgent::class.java) { createCoupleFriendshipAgent(client, context) }
            }
        }
    }

    fun getReport(agentName: Class<out Agent<*, *>>): Map<String, List<Agent.Report>> {
        return agentReports[agentName.simpleName] ?: throw RuntimeException("Agent ${agentName.simpleName} has no report")
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
        LOGGER.info("Simulation run duration: " + printDuration(start, Instant.now()))
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

    protected abstract fun createPersonAgent(client: CLIENT, context: Context): PersonAgent<TX>
    protected abstract fun createFriendshipAgent(client: CLIENT, context: Context): FriendshipAgent<TX>
    protected abstract fun createMarriageAgent(client: CLIENT, context: Context): MarriageAgent<TX>
    protected abstract fun createParenthoodAgent(client: CLIENT, context: Context): ParenthoodAgent<TX>
    protected abstract fun createLineageAgent(client: CLIENT, context: Context): LineageAgent<TX>
    protected abstract fun createNationalityAgent(client: CLIENT, context: Context): NationalityAgent<TX>
    protected abstract fun createCitizenshipAgent(client: CLIENT, context: Context): CitizenshipAgent<TX>
    protected abstract fun createMaritalStatusAgent(client: CLIENT, context: Context): MaritalStatusAgent<TX>
    protected abstract fun createCoupleFriendshipAgent(client: CLIENT, context: Context): CoupleFriendshipAgent<TX>
    protected abstract fun createGrandparenthoodAgent(client: CLIENT, context: Context): GrandparenthoodAgent<TX>

    companion object {
        val REGISTERED_AGENTS: MutableSet<Class<out Agent<*, *>>> = mutableSetOf()
        private val LOGGER = KotlinLogging.logger {}
        private val AGENT_PACKAGE = Agent::class.java.packageName
    }
}
