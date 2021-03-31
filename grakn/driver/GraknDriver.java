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
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.driver.TransactionalDbDriver;
import grakn.benchmark.common.world.Region;
import grakn.client.Grakn;
import grakn.client.api.GraknClient;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.benchmark.common.driver.TransactionalDbDriver.TracingLabel.OPEN_SESSION;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknDriver extends TransactionalDbDriver<GraknTransaction, GraknSession, GraknOperation> {

    private final GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, GraknSession> sessionMap = new ConcurrentHashMap<>();

    public static GraknDriver core(String hostUri, String database) {
        return new GraknDriver(Grakn.coreClient(hostUri), database);
    }

    public static GraknDriver cluster(String hostUri, String database) {
        return new GraknDriver(Grakn.clusterClient(hostUri), database);
    }

    private GraknDriver(GraknClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public void createDatabase() {
        if (client.databases().contains(database)) client.databases().get(database).delete();
        client.databases().create(database);
    }

    @Override
    public GraknSession session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, GraknSession.Type.DATA);
            }
        });
    }

    public GraknSession schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, GraknSession.Type.SCHEMA);
            }
        });
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
        client.close();
    }

    @Override
    public void printStatistics(Logger LOG) {
        GraknSession session = session("statisticsDataSession");
        GraknTransaction tx = session.transaction(GraknTransaction.Type.READ);
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
    public DbOperationFactory<GraknOperation> getDbOperationFactory(Region region, Logger logger) {
        return new GraknOperationFactory(session(region.topLevelName()), logger);
    }
}
