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
import grakn.client.Grakn;
import grakn.client.GraknClient;
import grakn.common.collection.Either;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class GraknDriver extends TransactionalDbDriver<Grakn.Transaction, Grakn.Session, GraknOperation> {

    private final Either<Grakn.Client, GraknClient.Cluster> client;
    private final String database;
    private final ConcurrentHashMap<String, Grakn.Session> sessionMap = new ConcurrentHashMap<>();

    public static GraknDriver core(String hostUri, String database) {
        return new GraknDriver(Either.first(GraknClient.core(hostUri)), database);
    }

    public static GraknDriver cluster(String hostUri, String database) {
        return new GraknDriver(Either.second(GraknClient.cluster(hostUri)), database);
    }

    public GraknDriver(Either<Grakn.Client, GraknClient.Cluster> client, String database) {
        this.client = client;
        this.database = database;
    }

    public void createDatabase() {
        if (client.apply(
                core -> core.databases().contains(database),
                cluster -> cluster.databases().contains(database))
        )
            client.apply(
                    core -> { core.databases().delete(database); return null; },
                    cluster -> { cluster.databases().delete(database); return null; }
            );
        client.apply(
                core -> { core.databases().create(database); return null; },
                cluster -> { cluster.databases().create(database); return null; }
        );
    }

    @Override
    public Grakn.Session session(String sessionKey) {
        return sessionMap.computeIfAbsent(
                sessionKey,
                k ->
                        client.apply(
                                core -> core.session(database, Grakn.Session.Type.DATA),
                                cluster -> cluster.session(database, Grakn.Session.Type.DATA)
                        )
        );
    }

    public Grakn.Session schemaSession(String sessionKey) {
        return sessionMap.computeIfAbsent(
                sessionKey,
                k ->
                        client.apply(
                                core -> core.session(database, Grakn.Session.Type.SCHEMA),
                                cluster -> cluster.session(database, Grakn.Session.Type.SCHEMA)
                        )
        );
    }

    @Override
    public void closeSessions() {
        for (Grakn.Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        client.apply(core -> { core.close(); return null; }, cluster -> { cluster.close(); return null; });
    }

    @Override
    public DbOperationFactory<GraknOperation> getDbOperationFactory(Region region, Logger logger) {
        return new GraknOperationFactory(session(region.topLevelName()), logger);
    }
}
