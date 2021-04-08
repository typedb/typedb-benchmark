/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.neo4j.driver;

import grakn.benchmark.simulation.driver.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Neo4jTransaction implements Transaction {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4jTransaction.class);
    private final Session session;
    private final String tracker;
    private final long iteration;

    public Neo4jTransaction(Session session, String tracker, long iteration) {
        this.session = session;
        this.tracker = tracker;
        this.iteration = iteration;
    }

    public List<Record> execute(Query query) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        return session.writeTransaction(tx -> {
            Result result = tx.run(query);
            return result.list();
        });
    }

    public <T> List<T> sortedExecute(Query query, String attributeName, Integer limit) {
        LOG.debug("{}/{}:\n{}", iteration, tracker, query);
        Stream<T> answerStream = execute(query).stream()
                .map(record -> (T) record.asMap().get(attributeName))
                .sorted();
        if (limit != null) {
            answerStream = answerStream.limit(limit);
        }
        return answerStream.collect(Collectors.toList());
    }

    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    @Override
    public void commit() {}

    /**
     * Not necessary when using Neo4j's Transaction Functions
     */
    @Override
    public void close() {}
}
