package grakn.simulation.db.grakn.context;

import grakn.client.GraknClient;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.grakn.driver.Transaction;

import java.util.concurrent.ConcurrentHashMap;

public class GraknContext extends DatabaseContext {

    private final GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, GraknClient.Session> sessionMap = new ConcurrentHashMap<>();

    public GraknContext(String hostUri, String database) {
        this.client = new GraknClient(hostUri);
        this.database = database;
    }

    public GraknClient.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> client.session(database));
    }

    public Transaction tx(String sessionKey) {
        return new Transaction(session(sessionKey).transaction(GraknClient.Transaction.Type.WRITE));
    }

    public void closeSessions() {
        for (GraknClient.Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }

    public void close() {
        closeSessions();
        client.close();
    }
}
