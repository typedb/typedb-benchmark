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

package grakn.simulation.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.Grakn;
import grakn.client.rpc.GraknClient;
import grakn.simulation.common.driver.TransactionalDbDriver;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.Region;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_CLIENT;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.CLOSE_SESSION;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.OPEN_CLIENT;
import static grakn.simulation.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;

public class GraknDriver extends TransactionalDbDriver<Grakn.Transaction, Grakn.Session, GraknOperation> {

    private final Grakn.Client client;
    private final String database;
    private final ConcurrentHashMap<String, Grakn.Session> sessionMap = new ConcurrentHashMap<>();

    public GraknDriver(String hostUri, String database) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_CLIENT.getName())) {
            System.out.println("DEBUG LOGS hostUri" + hostUri + "DEBUG LOGS trace" + trace.toString());
            this.client = new GraknClient(hostUri);
        }
        this.database = database;
    }

    public void createDatabase() {
        if (client.databases().contains(database))
            client.databases().delete(database);
        client.databases().create(database);
    }

    @Override
    public Grakn.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, Grakn.Session.Type.DATA);
            }
        });
    }

    public Grakn.Session schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, Grakn.Session.Type.SCHEMA);
            }
        });
    }

    @Override
    public void closeSessions() {
        for (Grakn.Session session : sessionMap.values()) {
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
