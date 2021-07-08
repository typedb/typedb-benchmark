/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.neo4j.driver;

import com.vaticle.typedb.benchmark.simulation.driver.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class Neo4jTransaction implements Transaction {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4jTransaction.class);

    protected final Session session;

    public Neo4jTransaction(Session session) {
        // TODO: why are we not passing a Transaction in here?
        this.session = session;
    }

    static class Read extends Neo4jTransaction {

        public Read(Session session) {
            super(session);
        }

        public List<Record> execute(Query query) {
            return session.readTransaction(tx -> tx.run(query).list());
        }

        @Override
        public void commit() {
            throw new UnsupportedOperationException("There should not be a call to .commit() for read transactions.");
        }
    }

    static class Write extends Neo4jTransaction {

        public Write(Session session) {
            super(session);
        }

        public List<Record> execute(Query query) {
            return session.writeTransaction(tx -> tx.run(query).list());
        }

        /**
         * Not necessary when using Neo4j's Transaction Functions
         */
        @Override
        public void commit() {}
    }

    public List<Record> execute(Query query) {
        return session.writeTransaction(tx -> {
            Result result = tx.run(query);
            return result.list();
        });
    }


    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    @Override
    public void close() {}
}
