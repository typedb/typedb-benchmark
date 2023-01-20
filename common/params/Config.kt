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

import com.vaticle.typedb.benchmark.common.params.Config.Keys.AGENTS
import com.vaticle.typedb.benchmark.common.params.Config.Keys.AGE_OF_ADULTHOOD
import com.vaticle.typedb.benchmark.common.params.Config.Keys.AGE_OF_FRIENDSHIP
import com.vaticle.typedb.benchmark.common.params.Config.Keys.ARG
import com.vaticle.typedb.benchmark.common.params.Config.Keys.DATABASE_NAME
import com.vaticle.typedb.benchmark.common.params.Config.Keys.FUNCTION
import com.vaticle.typedb.benchmark.common.params.Config.Keys.IS_ENABLED
import com.vaticle.typedb.benchmark.common.params.Config.Keys.ITERATIONS
import com.vaticle.typedb.benchmark.common.params.Config.Keys.MODEL
import com.vaticle.typedb.benchmark.common.params.Config.Keys.NAME
import com.vaticle.typedb.benchmark.common.params.Config.Keys.RANDOM_SEED
import com.vaticle.typedb.benchmark.common.params.Config.Keys.RUN
import com.vaticle.typedb.benchmark.common.params.Config.Keys.SCALE_FACTOR
import com.vaticle.typedb.benchmark.common.params.Config.Keys.TRACE
import com.vaticle.typedb.benchmark.common.params.Config.Keys.TRACE_SAMPLING
import com.vaticle.typedb.benchmark.common.params.Config.Keys.YEARS_BEFORE_PARENTHOOD
import com.vaticle.typedb.common.yaml.YAML
import java.io.File
import kotlin.math.ln

class Config(val agents: List<Agent>, val traceSampling: TraceSampling?, val run: Run, val model: Model) {

    class Agent(val name: String, val isEnabled: Boolean, val trace: Boolean) {

        companion object {
            internal fun of(yaml: YAML.Map) = Agent(
                name = yaml[NAME].asString().value(),
                isEnabled = yaml[IS_ENABLED].asBoolean().value(),
                trace = yaml[TRACE].asBoolean().value()
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
                _function = SamplingFunction.valueOf(yaml[FUNCTION].asString().value().uppercase()),
                arg = yaml[ARG].asInt().value()
            )
        }
    }

    class Run(val randomSeed: Long, val iterations: Int, val scaleFactor: Int, val databaseName: String) {
        companion object {
            internal fun of(yaml: YAML.Map) = Run(
                databaseName = yaml[DATABASE_NAME].asString().value(),
                iterations = yaml[ITERATIONS].asInt().value(),
                scaleFactor = yaml[SCALE_FACTOR].asInt().value(),
                randomSeed = yaml[RANDOM_SEED].asInt().value().toLong(),
            )
        }
    }

    class Model(val ageOfFriendship: Int, val ageOfAdulthood: Int, val yearsBeforeParenthood: Int) {
        companion object {
            internal fun of(yaml: YAML.Map) = Model(
                ageOfAdulthood = yaml[AGE_OF_ADULTHOOD].asInt().value(),
                ageOfFriendship = yaml[AGE_OF_FRIENDSHIP].asInt().value(),
                yearsBeforeParenthood = yaml[YEARS_BEFORE_PARENTHOOD].asInt().value(),
            )
        }
    }

    companion object {
        fun of(file: File): Config = of(YAML.load(file.toPath()).asMap())

        fun of(yaml: YAML.Map): Config {
            return Config(
                agents = yaml[AGENTS].asList().content().map { Agent.of(it.asMap()) },
                traceSampling = yaml[TRACE_SAMPLING]?.let { TraceSampling.of(it.asMap()) },
                run = Run.of(yaml[RUN].asMap()),
                model = Model.of(yaml[MODEL].asMap())
            )
        }
    }

    internal object Keys {
        const val AGE_OF_ADULTHOOD = "ageOfAdulthood"
        const val AGE_OF_FRIENDSHIP = "ageOfFriendship"
        const val AGENTS = "agents"
        const val ARG = "arg"
        const val DATABASE_NAME = "databaseName"
        const val FUNCTION = "function"
        const val IS_ENABLED = "isEnabled"
        const val ITERATIONS = "iterations"
        const val MODEL = "model"
        const val NAME = "name"
        const val RANDOM_SEED = "randomSeed"
        const val RUN = "run"
        const val SCALE_FACTOR = "scaleFactor"
        const val TRACE = "trace"
        const val TRACE_SAMPLING = "traceSampling"
        const val YEARS_BEFORE_PARENTHOOD = "yearsBeforeParenthood"
    }
}
