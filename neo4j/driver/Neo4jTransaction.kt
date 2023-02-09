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

import com.vaticle.typedb.simulation.common.driver.Transaction
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import org.neo4j.driver.Session

// TODO: why are we not passing a Transaction in here?
abstract class Neo4jTransaction(protected val session: Session) : Transaction {
    abstract fun execute(query: Query): List<Record>

    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    override fun close() {}
    internal class Read(session: Session) : Neo4jTransaction(session) {
        override fun execute(query: Query): List<Record> {
            return session.readTransaction { tx -> tx.run(query).list() }
        }

        override fun commit() {
            throw UnsupportedOperationException("There should not be a call to .commit() for read transactions.")
        }
    }

    internal class Write(session: Session) : Neo4jTransaction(session) {
        override fun execute(query: Query): List<Record> {
            return session.writeTransaction { tx -> tx.run(query).list() }
        }

        /**
         * Not necessary when using Neo4j's Transaction Functions
         */
        override fun commit() {}
    }
}
