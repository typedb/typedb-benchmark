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

import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.driver.TransactionalDbDriver;
import grakn.benchmark.common.world.Region;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlMatch;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class GraknDriver extends TransactionalDbDriver<GraknClient.Transaction, GraknClient.Session, GraknOperation> {

    private final GraknClient client;
    private final String database;
    private final ConcurrentHashMap<String, GraknClient.Session> sessionMap = new ConcurrentHashMap<>();

    public static GraknDriver core(String hostUri, String database) {
        return new GraknDriver(GraknClient.core(hostUri), database);
    }

    public static GraknDriver cluster(String hostUri, String database) {
        return new GraknDriver(GraknClient.cluster(hostUri), database);
    }

    private GraknDriver(GraknClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public void createDatabase() {
        if (client.databases().contains(database))
            client.databases().delete(database);
        client.databases().create(database);
    }

    @Override
    public GraknClient.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, GraknClient.Session.Type.DATA);
            }
        });
    }

    public GraknClient.Session schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(sessionKey, k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return client.session(database, GraknClient.Session.Type.SCHEMA);
            }
        });
    }

    @Override
    public void closeSessions() {
        for (GraknClient.Session session : sessionMap.values()) {
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
        GraknClient.Session session = session("statisticsDataSession");
        GraknClient.Transaction tx = session.transaction(GraknClient.Transaction.Type.READ);

        GraqlMatch.Unfiltered.Aggregate numberOfEntitiesQ = Graql.match(
                Graql.var("x").isa("entity")
        ).get("x").count();
        long numberOfEntities = tx.query().match(numberOfEntitiesQ).get().asLong();

        GraqlMatch.Unfiltered.Aggregate numberOfAttributesQ = Graql.match(
                Graql.var("x").isa("attribute")
        ).get("x").count();
        long numberOfAttributes = tx.query().match(numberOfAttributesQ).get().asLong();

        GraqlMatch.Unfiltered.Aggregate numberOfRelationsQ = Graql.match(
                Graql.var("x").isa("relation")
        ).get("x").count();
        long numberOfRelations = tx.query().match(numberOfRelationsQ).get().asLong();

        GraqlMatch.Unfiltered.Aggregate numberOfThingsQ = Graql.match(
                Graql.var("x").isa("thing")
        ).get("x").count();
        long numberOfThings = tx.query().match(numberOfThingsQ).get().asLong();


        LOG.info("");

        LOG.info("Benchmark statistic:");

        LOG.info("");

        LOG.info("Total 'entity' count: '{}'.", numberOfEntities);
        LOG.info("Total 'attribute' count: '{}'.", numberOfAttributes);
        LOG.info("Total 'relation' count: '{}'.", numberOfRelations);
        LOG.info("Total count: '{}'.", numberOfEntities + numberOfAttributes + numberOfRelations);

        LOG.info("");

        LOG.info("Total 'thing' count: '{}'.", numberOfThings);

        LOG.info("");
    }

    @Override
    public DbOperationFactory<GraknOperation> getDbOperationFactory(Region region, Logger logger) {
        return new GraknOperationFactory(session(region.topLevelName()), logger);
    }
}
