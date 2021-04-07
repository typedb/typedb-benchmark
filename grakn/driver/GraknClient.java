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

package grakn.benchmark.grakn.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.simulation.driver.Client;
import grakn.client.Grakn;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.benchmark.simulation.driver.Client.TracingLabel.OPEN_SESSION;
import static grakn.client.api.GraknTransaction.Type.READ;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknClient extends Client<GraknSession, GraknTransaction> {

    private final grakn.client.api.GraknClient nativeClient;
    private final String database;
    private final ConcurrentHashMap<String, GraknSession> sessionMap = new ConcurrentHashMap<>();

    public static GraknClient core(String hostUri, String database) {
        return new GraknClient(Grakn.coreClient(hostUri), database);
    }

    public static GraknClient cluster(String hostUri, String database) {
        return new GraknClient(Grakn.clusterClient(hostUri), database);
    }

    private GraknClient(grakn.client.api.GraknClient nativeClient, String database) {
        this.nativeClient = nativeClient;
        this.database = database;
    }

    public void createDatabase() {
        if (nativeClient.databases().contains(database)) nativeClient.databases().get(database).delete();
        nativeClient.databases().create(database);
    }

    @Override
    public GraknSession session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return new GraknSession(nativeClient.session(database, grakn.client.api.GraknSession.Type.DATA));
            }
        });
    }

    public GraknSession schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return new GraknSession(nativeClient.session(database, grakn.client.api.GraknSession.Type.SCHEMA));
            }
        });
    }

    @Override
    public String printStatistics() {
        StringBuilder str = new StringBuilder();
        try (GraknSession session = session("statisticsDataSession")) {
            grakn.client.api.GraknSession nativeSession = session.unpack();
            try (grakn.client.api.GraknTransaction tx = nativeSession.transaction(READ)) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                long numberOfEntities = tx.query().match(match(var("x").isa("entity")).count()).get().asLong();
                long numberOfAttributes = tx.query().match(match(var("x").isa("attribute")).count()).get().asLong();
                long numberOfRelations = tx.query().match(match(var("x").isa("relation")).count()).get().asLong();
                long numberOfThings = tx.query().match(match(var("x").isa("thing")).count()).get().asLong();

                str.append("Benchmark statistic:");
                str.append("\n");
                str.append("Count 'entity': {}").append(formatter.format(numberOfEntities));
                str.append("Count 'relation': {}").append(formatter.format(numberOfRelations));
                str.append("Count 'attribute': {}").append(formatter.format(numberOfAttributes));
                if (numberOfThings != numberOfEntities + numberOfAttributes + numberOfRelations) {
                    str.append("The sum of 'entity', 'relation', and 'attribute' counts do not match the total 'thing' count: {}").append(formatter.format(numberOfThings));
                } else {
                    str.append("Count 'thing' (total): {}").append(formatter.format(numberOfThings));
                }
                str.append("\n");
            }
        }
        return str.toString();
    }

    @Override
    public void closeSessions() {
        for (GraknSession session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        nativeClient.close();
    }
}
