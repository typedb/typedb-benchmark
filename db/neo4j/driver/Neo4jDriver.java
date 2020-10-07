package grakn.simulation.db.neo4j.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.driver.TransactionalDbDriver;
import grakn.simulation.db.common.world.Region;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_TRANSACTION;

public class Neo4jDriver extends TransactionalDbDriver<Neo4jTransaction, org.neo4j.driver.Session> {

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
    public DbOperationFactory<DB_OPERATION> getDbOperationFactory(Region region, Logger logger) {
        return new Neo4jOperationFactory(new Neo4jDriver.Neo4jSession(session(region.continent().name())), logger);
    }

    public class Neo4jSession extends Session {
        private final org.neo4j.driver.Session session;

        public Neo4jSession(org.neo4j.driver.Session session) {
            this.session = session;
        }

        @Override
        public Neo4jTransaction tx() {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
                return new Neo4jTransaction(session, null, null);
            }
        }
    }
}
