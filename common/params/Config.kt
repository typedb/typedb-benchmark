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

import com.vaticle.typedb.common.yaml.YAML
import java.io.File
import kotlin.math.ln

class Config(val agents: List<Agent>, val traceSampling: TraceSampling?, val run: Run, val model: Model) {

    class Agent(val name: String, val isEnabled: Boolean, val trace: Boolean) {

        companion object {
            internal fun of(yaml: YAML.Map) = Agent(
                name = yaml["name"].asString().value(),
                isEnabled = yaml["isEnabled"].asBoolean().value(),
                trace = yaml["trace"].asBoolean().value()
            )
        }
    }

    class TraceSampling(private val _function: SamplingFunction, private val arg: Int) {

        val function get(): (Int) -> Boolean {
            return _function.applyArg(arg)
        }

        enum class SamplingFunction {
            EVERY, LOG;

            fun applyArg(arg: Int): (Int) -> Boolean {
                return when (this) {
                    EVERY -> {
                        require(arg >= 1) { "`every` requires an argument of 1 or greater" };
                        { i -> i % arg == 0 }
                    }
                    LOG -> {
                        require(arg >= 2) { "`log` requires a base of 2 or greater" };
                        // return true if the logarithm of `i` to the given base is an integer
                        { i -> ln(i.toDouble()).toInt() / ln(arg.toDouble()) % 1 == 0.0 }
                    }
                }
            }
        }

        companion object {
            internal fun of(yaml: YAML.Map) = TraceSampling(
                _function = SamplingFunction.valueOf(yaml["function"].asString().value().uppercase()),
                arg = yaml["arg"].asInt().value()
            )
        }
    }

    class Run(val randomSeed: Long, val iterations: Int, val scaleFactor: Int, val databaseName: String) {
        companion object {
            internal fun of(yaml: YAML.Map) = Run(
                databaseName = yaml["databaseName"].asString().value(),
                iterations = yaml["iterations"].asInt().value(),
                scaleFactor = yaml["scaleFactor"].asInt().value(),
                randomSeed = yaml["randomSeed"].asInt().value().toLong(),
            )
        }
    }

    class Model(val ageOfFriendship: Int, val ageOfAdulthood: Int, val yearsBeforeParenthood: Int) {
        companion object {
            internal fun of(yaml: YAML.Map) = Model(
                ageOfAdulthood = yaml["ageOfAdulthood"].asInt().value(),
                ageOfFriendship = yaml["ageOfFriendship"].asInt().value(),
                yearsBeforeParenthood = yaml["yearsBeforeParenthood"].asInt().value(),
            )
        }
    }

    companion object {
        fun of(file: File): Config = of(YAML.load(file.toPath()).asMap())

        fun of(yaml: YAML.Map): Config {
            return Config(
                agents = yaml["agents"].asList().content().map { Agent.of(it.asMap()) },
                traceSampling = yaml["traceSampling"]?.let { TraceSampling.of(it.asMap()) },
                run = Run.of(yaml["run"].asMap()),
                model = Model.of(yaml["model"].asMap())
            )
        }
    }
}
