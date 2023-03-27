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

import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic
import com.vaticle.typedb.simulation.common.Partition
import com.vaticle.typedb.simulation.common.seed.RandomSource
import com.vaticle.typedb.simulation.common.DBClient
import com.vaticle.typedb.common.util.Objects.className
import com.vaticle.typedb.simulation.common.params.Config.Agent.Companion.DEFAULT_ACTION
import com.vaticle.typedb.simulation.common.params.Config.Agent.Companion.DEFAULT_RUNS_PER_ITERATION
import java.util.Objects.hash
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and TypeDB transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <PARTITION> The type of region used by the agent.
 * @param <TX>     The abstraction of database operations used by the agent.
</TX></REGION> */
abstract class Agent<PARTITION: Partition, SESSION, CONTEXT: Context<*, *>> protected constructor(
    private val client: DBClient<SESSION>, protected val context: CONTEXT
) {
    var action = DEFAULT_ACTION
    var runsPerIteration = DEFAULT_RUNS_PER_ITERATION
    var tracingEnabled = true
    protected abstract val agentClass: Class<out Agent<*, *, *>>
    protected abstract val partitions: List<PARTITION>

    protected abstract val actionHandlers: Map<String, (SESSION, PARTITION, RandomSource) -> List<Report>>

    fun run(session: SESSION, partition: PARTITION, random: RandomSource): List<Report> {
        return actionHandlers[action]?.let {
            (0 until runsPerIteration).flatMap { it(session, partition, random.nextSource()) }
        } ?: throw IllegalArgumentException("The action '$action' has no registered handler in '${javaClass.simpleName}'"
                + if (action == DEFAULT_ACTION) " (help: '$action' is the default action)" else "")
    }

    private fun shouldTrace(): Boolean {
        return context.isTracing && this.tracingEnabled
    }

    fun iterate(randomSource: RandomSource): Map<String, List<Report>> {
        val reports = ConcurrentHashMap<String, List<Report>>()
        // We need to generate pairs of Partition and Random deterministically before passing them to a parallel stream
        if (context.partitionCount > partitions.size) throw IllegalArgumentException("Partition count exceeds supplied number of partitions.")
        val validPartitions = partitions.subList(0, context.partitionCount)
        val asyncRuns = validPartitions.map { partition ->
            CompletableFuture.runAsync(
                {
                    val report = runAndMayTrace(partition, randomSource)
                    if (context.isReporting) reports[partition.tracker] = report else assert(report.isEmpty())
                }, context.executor
            )
        }
        CompletableFuture.allOf(*asyncRuns.toTypedArray<CompletableFuture<*>>()).join()
        return reports
    }

    private fun runAndMayTrace(partition: PARTITION, randomSource: RandomSource): List<Report> {
        var tracingCtx: FactoryTracingThreadStatic.ThreadContext? = null
        return try {
            if (shouldTrace()) tracingCtx =
                FactoryTracingThreadStatic.contextOnThread(partition.tracker, context.iterationNumber)
            val session = client.session(partition)
            mayTrace(className(agentClass)) { run(session, partition, randomSource) }
        } finally {
            tracingCtx?.close()
        }
    }

    private fun <T> mayTrace(trace: String, methodToTrace: Supplier<T>): T {
        if (shouldTrace()) {
            FactoryTracingThreadStatic.traceOnThread(trace).use { return methodToTrace.get() }
        } else {
            return methodToTrace.get()
        }
    }

    abstract class Factory {

        protected abstract val map: Map<Class<out Agent<*, *, *>>, () -> Agent<*, *, *>>

        operator fun get(key: Class<out Agent<*, *, *>>) = map[key]
    }

    class Report(private val input: Collection<Any>, private val output: Collection<Any>) {
        private val hash = hash(input, output)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val report = other as Report
            return if (input != report.input) false else output == report.output
        }

        override fun hashCode(): Int {
            return hash
        }
    }
}
