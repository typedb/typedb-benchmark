/*
 * Copyright (C) 2020 Grakn Labs
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

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.driver.TransactionalDbDriver;
import grakn.benchmark.common.world.Region;
import org.neo4j.driver.*;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.benchmark.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;

public class Neo4jDriver extends TransactionalDbDriver<org.neo4j.driver.Transaction, org.neo4j.driver.Session, Neo4jOperation> {

    private final Driver driver;
    private final ConcurrentHashMap<String, org.neo4j.driver.Session> sessionMap = new ConcurrentHashMap<>();

    public Neo4jDriver(String hostUri) {
        this.driver = GraphDatabase.driver(hostUri, AuthTokens.basic("neo4j", "admin"));
    }

    @Override
    public org.neo4j.driver.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return driver.session();
            }
        });
    }

    @Override
    public void closeSessions() {
        for (org.neo4j.driver.Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        driver.close();
    }

    @Override
    public void printStatistics(Logger LOG) {
        org.neo4j.driver.Session session = session("statisticsDataSession");

        String numberOfNodesQ = "MATCH (n)\n RETURN count(n)";
        List<Record> numberOfNodesList = session.writeTransaction(tx -> {
            Result result = tx.run(new Query(numberOfNodesQ));
            return result.list();
        });
        long numberOfNodes = (long) getOnlyElement(numberOfNodesList).asMap().get("count(n)");

        String numberOfRelationshipsQ = "MATCH ()-->()\n RETURN count(*)";
        List<Record> numberOfRelationshipsList = session.writeTransaction(tx -> {
            Result result = tx.run(new Query(numberOfRelationshipsQ));
            return result.list();
        });
        long numberOfRelationships = (long) getOnlyElement(numberOfRelationshipsList).asMap().get("count(*)");

        LOG.info("\n");

        LOG.info("Benchmark statistic.");

        LOG.info("\n");

        LOG.info("Number of 'node' elements: '{}'.", numberOfNodes);
        LOG.info("Number of 'relationship' elements: '{}'.", numberOfRelationships);
        LOG.info("Combined: '{}'.", numberOfNodes + numberOfRelationships);

        LOG.info("\n");
    }

    @Override
    public DbOperationFactory<Neo4jOperation> getDbOperationFactory(Region region, Logger logger) {
        return new Neo4jOperationFactory(session(region.name()), logger);
    }
}
