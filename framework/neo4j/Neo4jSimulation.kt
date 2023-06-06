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
package com.vaticle.typedb.benchmark.framework.neo4j

import com.vaticle.typedb.benchmark.framework.Agent
import com.vaticle.typedb.benchmark.framework.common.Util.printDuration
import com.vaticle.typedb.benchmark.framework.neo4j.Keywords.DELETE
import com.vaticle.typedb.benchmark.framework.neo4j.Keywords.DETACH
import com.vaticle.typedb.benchmark.framework.neo4j.Keywords.MATCH
import com.vaticle.typedb.benchmark.framework.Context
import com.vaticle.typedb.benchmark.framework.Simulation
import com.vaticle.typedb.benchmark.framework.common.seed.RandomSource
import mu.KotlinLogging
import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import java.time.Instant

abstract class Neo4jSimulation<out CONTEXT: Context<*, *>> protected constructor(
    client: Neo4jClient, context: CONTEXT, agentFactory: Agent.Factory
) : Simulation<Neo4jClient, CONTEXT>(client, context, agentFactory) {

    override fun init(randomSource: RandomSource) {
        val nativeDriver = client.unpack()
        initDatabase(nativeDriver)
        LOGGER.info("Neo4j initialisation of $name simulation data started ...")
        val start = Instant.now()
        initData(nativeDriver, randomSource)
        LOGGER.info("Neo4j initialisation of $name simulation data ended in: {}", printDuration(start, Instant.now()))
    }

    private fun initDatabase(nativeDriver: Driver) {
        nativeDriver.session().use { session ->
            addKeyConstraints(session)
            cleanDatabase(session)
        }
    }

    /**
     * Neo4j Community can create only uniqueness constraints, and only on nodes, not relationships. This means that it
     * does not enforce the existence of a property on those nodes. `exists()` is only available in Neo4j Enterprise.
     * https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
     *
     * @param session
     */
    protected abstract fun addKeyConstraints(session: Session)

    private fun cleanDatabase(session: Session) {
        val tx = session.beginTransaction()
        tx.run(Query("$MATCH (n) $DETACH $DELETE n"))
        tx.commit()
    }

    protected abstract fun initData(nativeDriver: Driver, randomSource: RandomSource)

    protected fun escapeQuotes(string: String): String {
        return string.replace("'", "\\'")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun unsupportedReasoningAgentException(agentName: String): UnsupportedOperationException {
            return UnsupportedOperationException("$agentName requires reasoning, which is not supported by Neo4j")
        }
    }
}
