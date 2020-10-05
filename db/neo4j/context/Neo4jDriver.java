package grakn.simulation.db.neo4j.context;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.context.LogWrapper;
import grakn.simulation.db.common.context.TransactionalDbDriver;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.grakn.agents.interaction.GraknDbOperationController;
import grakn.simulation.db.grakn.context.GraknDriver;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import grakn.simulation.db.neo4j.agents.interaction.Neo4jDbOperationController;
import grakn.simulation.db.neo4j.driver.Neo4jTransaction;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.context.TransactionalDbDriver.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.db.common.context.TransactionalDbDriver.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.context.TransactionalDbDriver.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.context.TransactionalDbDriver.TracingLabel.OPEN_SESSION;
import static grakn.simulation.db.common.context.TransactionalDbDriver.TracingLabel.OPEN_TRANSACTION;

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
    public DbOperationController getDbOpController(Region region, Logger logger) {
        return new Neo4jDbOperationController(new Neo4jDriver.Neo4jSession(session(region.continent().name())), logger);
    }

    public class Neo4jSession extends Session {
        private final org.neo4j.driver.Session session;

        public Neo4jSession(org.neo4j.driver.Session session) {
            this.session = session;
        }

        @Override
        public Neo4jTransaction tx(LogWrapper log, String tracker) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
                return new Neo4jTransaction(session, log, tracker);
            }
        }
    }
}
