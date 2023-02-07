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
package com.vaticle.typedb.benchmark.neo4j.driver

import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction.Read
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction.Write
import org.neo4j.driver.Session

class Neo4jSession(private val nativeSession: Session) :
    com.vaticle.typedb.benchmark.simulation.driver.Session<Neo4jTransaction> {
    override fun writeTransaction(): Neo4jTransaction {
        return Write(nativeSession)
    }

    override fun readTransaction(): Neo4jTransaction {
        return Read(nativeSession)
    }

    override fun reasoningTransaction(): Neo4jTransaction {
        throw UnsupportedOperationException("Neo4j does not support reasoning transactions")
    }

    override fun close() {
        nativeSession.close()
    }
}
