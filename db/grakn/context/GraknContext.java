package grakn.simulation.db.grakn.context;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.db.common.context.LogWrapper;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.grakn.driver.Transaction;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_SESSION;
import static grakn.simulation.db.common.context.DatabaseContext.TracingLabel.OPEN_TRANSACTION;

public class GraknContext extends DatabaseContext<Transaction> {

    private final GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, GraknClient.Session> sessionMap = new ConcurrentHashMap<>();

    public GraknContext(String hostUri, String database) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            this.client = new GraknClient(hostUri);
        }
        this.database = database;
    }

    public GraknClient.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database);
            }
        });
    }

    public Transaction tx(String sessionKey, LogWrapper log, String tracker) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_TRANSACTION.getName())) {
            return new Transaction(session(sessionKey).transaction(GraknClient.Transaction.Type.WRITE), log, tracker);
        }
    }

    public void closeSessions() {
        for (GraknClient.Session session : sessionMap.values()) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_SESSION.getName())) {
                session.close();
            }
        }
        sessionMap.clear();
    }

    public void close() {
        closeSessions();
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(CLOSE_CLIENT.getName())) {
            client.close();
        }
    }
}
