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

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Locale
import java.util.function.Function
import kotlin.math.ln

class Config {
    var agents: List<Agent>? = null
    var traceSampling: TraceSampling? = null
    var runParams = DEFAULT_RUN_PARAMS
    var modelParams = DEFAULT_MODEL_PARAMS

    class RunParams {
        var randomSeed = DEFAULT_RANDOM_SEED
        var iterations = DEFAULT_NUM_ITERATIONS
        var scaleFactor = DEFAULT_SCALE_FACTOR
        var databaseName = DEFAULT_DATABASE_NAME

        companion object {
            private const val DEFAULT_RANDOM_SEED = 1
            private const val DEFAULT_NUM_ITERATIONS = 10
            private const val DEFAULT_SCALE_FACTOR = 5
            private const val DEFAULT_DATABASE_NAME = "world"
        }
    }

    class ModelParams {
        var ageOfFriendship = DEFAULT_AGE_OF_FRIENDSHIP
        var ageOfAdulthood = DEFAULT_AGE_OF_ADULTHOOD
        var yearsBeforeParenthood = DEFAULT_YEARS_BEFORE_PARENTHOOD

        companion object {
            private const val DEFAULT_AGE_OF_FRIENDSHIP = 14
            private const val DEFAULT_AGE_OF_ADULTHOOD = 21
            private const val DEFAULT_YEARS_BEFORE_PARENTHOOD = 3
        }
    }

    class TraceSampling {
        private var function: SamplingFunction? = null
        private var arg: Int? = null
        fun samplingFunction(): Function<Int?, Boolean?> {
            return SamplingFunction.applyArg(function, arg)
        }

        fun setFunction(function: String) {
            this.function = SamplingFunction.getByName(function)
        }

        enum class SamplingFunction(private val acceptedArgs: IntArray) {
            EVERY(IntArray(0)), LOG(IntArray(0));

            fun acceptedArgs(): IntArray {
                return acceptedArgs
            }

            companion object {
                fun getByName(name: String): SamplingFunction {
                    for (samplingFunction in values()) {
                        if (samplingFunction.name.lowercase(Locale.getDefault()) == name.lowercase(Locale.getDefault())) {
                            return samplingFunction
                        }
                    }
                    throw IllegalArgumentException("Function name not recognised")
                }

                fun applyArg(sampling: SamplingFunction?, arg: Int?): Function<Int?, Boolean?> {
                    return when (sampling) {
                        EVERY -> {
                            require(arg!! >= 1) { "`every` requires an argument of 1 or greater" }
                            Function { i: Int? -> i!! % arg == 0 }
                        }

                        LOG -> {
                            require(arg!! >= 2) { "`log` requires a base of 2 or greater" }
                            // return true if the logarithm of `i` to the given base is an integer
                            Function { i: Int? ->
                                ln(i!!.toDouble()).toInt() / ln(arg.toDouble()) % 1 == 0.0
                            }
                        }

                        else -> throw IllegalStateException()
                    }
                }
            }
        }
    }

    class Agent {
        // TODO: mode should just be replaced with two booleans: 'run' and 'trace'
        private var mode: Mode? = null

        var name: String? = null

        @Suppress("unused")
        enum class Mode {
            TRACE, RUN, OFF
        }

        val isEnabled: Boolean
            get() = mode == Mode.RUN || mode == Mode.TRACE
        val isTracing: Boolean
            get() = mode == Mode.TRACE
    }

    companion object {
        private val DEFAULT_RUN_PARAMS = RunParams()
        private val DEFAULT_MODEL_PARAMS = ModelParams()

        fun loadYML(file: File): Config {
            val yaml = Yaml(Constructor(Config::class.java))
            return try {
                FileInputStream(file.toPath().toString()).use { yaml.load(it) }
            } catch (e: FileNotFoundException) {
                throw RuntimeException("Couldn't find config file")
            }
        }
    }
}
