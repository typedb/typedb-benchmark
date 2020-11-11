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
