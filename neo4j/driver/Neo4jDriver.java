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

package grakn.simulation.neo4j.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.driver.TransactionalDbDriver;
import grakn.simulation.common.world.Region;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;

public class Neo4jDriver extends TransactionalDbDriver<org.neo4j.driver.Transaction, org.neo4j.driver.Session, Neo4jOperation> {

    private final Driver driver;
    private final ConcurrentHashMap<String, org.neo4j.driver.Session> sessionMap = new ConcurrentHashMap<>();

    public Neo4jDriver(String hostUri) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            this.driver = GraphDatabase.driver(hostUri, AuthTokens.basic("neo4j", "admin"));
        }
    }

    @Override
    public org.neo4j.driver.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return driver.session();
            }
        });
    }

    @Override
    public void closeSessions() {
        for (org.neo4j.driver.Session session : sessionMap.values()) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_SESSION.getName())) {
                session.close();
            }
        }
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_CLIENT.getName())) {
            driver.close();
        }
    }

    @Override
    public DbOperationFactory<Neo4jOperation> getDbOperationFactory(Region region, Logger logger) {
        return new Neo4jOperationFactory(session(region.name()), logger);
    }
}
