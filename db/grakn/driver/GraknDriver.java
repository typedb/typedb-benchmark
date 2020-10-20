package grakn.simulation.db.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.db.common.driver.TransactionalDbDriver;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.Region;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.db.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;

public class GraknDriver extends TransactionalDbDriver<GraknClient.Transaction, GraknClient.Session, GraknOperation> {

    private final GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, GraknClient.Session> sessionMap = new ConcurrentHashMap<>();

    public GraknDriver(String hostUri, String database) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            this.client = new GraknClient(hostUri);
        }
        this.database = database;
    }

    @Override
    public GraknClient.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database);
            }
        });
    }

    @Override
    public void closeSessions() {
        for (GraknClient.Session session : sessionMap.values()) {
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
            client.close();
        }
    }

    @Override
    public DbOperationFactory<GraknOperation> getDbOperationFactory(Region region, Logger logger) {
        return new GraknOperationFactory(session(region.topLevelName()), logger);
    }
}
