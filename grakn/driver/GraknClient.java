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
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.TransactionalClient;
import grakn.benchmark.simulation.world.Region;
import grakn.client.Grakn;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.benchmark.simulation.driver.TransactionalClient.TracingLabel.OPEN_SESSION;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknClient extends TransactionalClient<grakn.client.api.GraknSession, GraknTransaction> {

    private final grakn.client.api.GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, grakn.client.api.GraknSession> sessionMap = new ConcurrentHashMap<>();

    public static GraknClient core(String hostUri, String database) {
        return new GraknClient(Grakn.coreClient(hostUri), database);
    }

    public static GraknClient cluster(String hostUri, String database) {
        return new GraknClient(Grakn.clusterClient(hostUri), database);
    }

    private GraknClient(grakn.client.api.GraknClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public void createDatabase() {
        if (client.databases().contains(database)) client.databases().get(database).delete();
        client.databases().create(database);
    }

    @Override
    public grakn.client.api.GraknSession session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, grakn.client.api.GraknSession.Type.DATA);
            }
        });
    }

    public grakn.client.api.GraknSession schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, grakn.client.api.GraknSession.Type.SCHEMA);
            }
        });
    }

    @Override
    public void closeSessions() {
        for (grakn.client.api.GraknSession session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        client.close();
    }

    @Override
    public void printStatistics(Logger LOG) {
        grakn.client.api.GraknSession session = session("statisticsDataSession");
        grakn.client.api.GraknTransaction tx = session.transaction(grakn.client.api.GraknTransaction.Type.READ);
        DecimalFormat formatter = new DecimalFormat("#,###");

        long numberOfEntities = tx.query().match(match(var("x").isa("entity")).count()).get().asLong();
        long numberOfAttributes = tx.query().match(match(var("x").isa("attribute")).count()).get().asLong();
        long numberOfRelations = tx.query().match(match(var("x").isa("relation")).count()).get().asLong();
        long numberOfThings = tx.query().match(match(var("x").isa("thing")).count()).get().asLong();

        LOG.info("");
        LOG.info("Benchmark statistic:");
        LOG.info("");
        LOG.info("Count 'entity': {}", formatter.format(numberOfEntities));
        LOG.info("Count 'relation': {}", formatter.format(numberOfRelations));
        LOG.info("Count 'attribute': {}", formatter.format(numberOfAttributes));
        if (numberOfThings != numberOfEntities + numberOfAttributes + numberOfRelations) {
            LOG.error("The sum of 'entity', 'relation', and 'attribute' counts do not match the total 'thing' count: {}", formatter.format(numberOfThings));
        } else {
            LOG.info("Count 'thing' (total): {}", formatter.format(numberOfThings));
        }
        LOG.info("");
    }

    @Override
    public Session<GraknTransaction> session(Region region, Logger logger) {
        return new GraknSession(session(region.topLevelName()), logger);
    }
}
