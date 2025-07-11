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
package com.vaticle.typedb.iam.simulation.test

import com.vaticle.typedb.iam.simulation.common.Config
import com.vaticle.typedb.iam.simulation.common.Context.Companion.create
import com.vaticle.typedb.iam.simulation.common.ModelParams
import com.vaticle.typedb.iam.simulation.neo4j.Neo4jSimulation
import com.vaticle.typedb.iam.simulation.neo4j.Neo4jSimulation.Companion.create
import com.vaticle.typedb.iam.simulation.typedb.TypeDBSimulation
import com.vaticle.typedb.benchmark.framework.Agent
import com.vaticle.typedb.benchmark.framework.common.params.Options.Companion.parseCLIOptions
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import picocli.CommandLine
import java.nio.file.Paths
import java.util.stream.Stream

@RunWith(ComparisonTest.Suite::class)
class ComparisonTest {
    @Test
    fun test_agents_have_equal_reports() {
        Suite.TYPEDB.registeredAgents.forEach { agent: Class<out Agent<*, *, *>> ->
            Assert.assertEquals(Suite.TYPEDB.getReport(agent), Suite.NEO4J.getReport(agent))
        }
    }

    class Suite(testClass: Class<*>) : org.junit.runners.Suite(testClass, createRunners(testClass)) {
        private var iteration = 1

        init {
            TYPEDB = TypeDBSimulation.core(OPTIONS.typeDBAddress, create(config = CONFIG, isTracing = false, isReporting = true))
            NEO4J = create(OPTIONS.neo4jAddress, create(config = CONFIG, isTracing = false, isReporting = true))
        }

        override fun runChild(runner: org.junit.runner.Runner, notifier: RunNotifier) {
            iteration++
            Stream.of(NEO4J, TYPEDB).parallel().forEach { it.iterate() }
            super.runChild(runner, notifier)
            if (iteration == CONFIG.run.iterations + 1) {
                TYPEDB.close()
                NEO4J.close()
            }
        }

        private class Runner(aClass: Class<*>?, private val iteration: Int) : BlockJUnit4ClassRunner(aClass) {
            override fun testName(method: FrameworkMethod): String {
                return "${method.name}-iter-$iteration"
            }

            override fun getName(): String {
                return "${super.getName()}-iter-$iteration"
            }
        }

        @CommandLine.Command(name = "benchmark-test", mixinStandardHelpOptions = true)
        private class Options {
            @CommandLine.Option(names = ["--typedb"], required = true, description = ["Database address URI"])
            lateinit var typeDBAddress: String

            @CommandLine.Option(names = ["--neo4j"], required = true, description = ["Database address URI"])
            lateinit var neo4jAddress: String
        }

        companion object {
            private val CONFIG = Config.of(Paths.get("simulation/test/comparison-test.yml").toFile()) { ModelParams.of(it) }
            private val OPTIONS = requireNotNull(parseCLIOptions(args(), Options()))
            lateinit var TYPEDB: TypeDBSimulation
            lateinit var NEO4J: Neo4jSimulation
            private fun args(): Array<String> {
                val input: Array<String> = System.getProperty("sun.java.command").split(" ").toTypedArray()
                return input.copyOfRange(1, input.size)
            }

            private fun createRunners(testClass: Class<*>): List<org.junit.runner.Runner> {
                val runners: MutableList<org.junit.runner.Runner> = ArrayList()
                for (i in 1..CONFIG.run.iterations) {
                    val runner: BlockJUnit4ClassRunner = Runner(testClass, i)
                    runners.add(runner)
                }
                return runners
            }
        }
    }
}
