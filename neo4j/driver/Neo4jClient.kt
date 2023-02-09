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
package com.vaticle.typedb.simulation.neo4j.driver

import com.vaticle.typedb.simulation.common.Partition
import com.vaticle.typedb.simulation.neo4j.Keywords.MATCH
import com.vaticle.typedb.simulation.neo4j.Keywords.RETURN
import com.vaticle.typedb.simulation.common.driver.Client
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Query
import org.neo4j.driver.Transaction
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

class Neo4jClient(hostUri: String) : Client<Neo4jSession> {
    private val nativeDriver = GraphDatabase.driver(hostUri, AuthTokens.basic("neo4j", "admin"))
    private val sessionMap = ConcurrentHashMap<String, Neo4jSession>()

    fun unpack(): Driver {
        return nativeDriver
    }

    override fun session(partition: Partition): Neo4jSession {
        return sessionMap.computeIfAbsent(partition.name) { Neo4jSession(nativeDriver.session()) }
    }

    override fun printStatistics(): String {
        val str = StringBuilder()
        nativeDriver.session().use { nativeSession ->
            val formatter = DecimalFormat("#,###")
            val numberOfNodesQ = "$MATCH (n)\n $RETURN count(n)"
            val numberOfNodesList = nativeSession.writeTransaction { tx: Transaction ->
                val result = tx.run(Query(numberOfNodesQ))
                result.list()
            }
            val numberOfNodes = numberOfNodesList[0].asMap()["count(n)"] as Long
            val numberOfRelationshipsQ = "$MATCH ()-->()\n $RETURN count(*)"
            val numberOfRelationshipsList = nativeSession.writeTransaction { tx: Transaction ->
                val result = tx.run(Query(numberOfRelationshipsQ))
                result.list()
            }
            val numberOfRelationships = numberOfRelationshipsList[0].asMap()["count(*)"] as Long
            str.append("Simulation statistic:").append("\n")
            str.append("\n")
            str.append("Count 'node': ").append(formatter.format(numberOfNodes)).append("\n")
            str.append("Count 'relationship': ").append(formatter.format(numberOfRelationships)).append("\n")
            str.append("\n")
        }
        return str.toString()
    }

    override fun closeSessions() {
        sessionMap.values.forEach { it.close() }
        sessionMap.clear()
    }

    override fun close() {
        closeSessions()
        nativeDriver.close()
    }
}
