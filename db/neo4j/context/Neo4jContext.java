package grakn.simulation.db.neo4j.context;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.agents.base.LogWrapper;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.neo4j.driver.Transaction;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_SESSION;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_TRANSACTION;

public class Neo4jContext extends DatabaseContext {

    private final Driver driver;
    private final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    public Neo4jContext(String hostUri) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            this.driver = GraphDatabase.driver(hostUri, AuthTokens.basic("neo4j", "admin"));
        }
    }

    public Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return driver.session();
            }
        });
    }

    public Transaction tx(String sessionKey, LogWrapper log, String tracker) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
            return new Transaction(session(sessionKey), log, tracker);
        }
    }

    public void closeSessions() {
        for (Session session : sessionMap.values()) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_SESSION.getName())) {
                session.close();
            }
        }
        sessionMap.clear();
    }

    public void close() {
        closeSessions();
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_CLIENT.getName())) {
            driver.close();
        }
    }
}
