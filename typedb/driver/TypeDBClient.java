/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.typedb.driver;

import com.vaticle.typedb.benchmark.common.concept.Region;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.client.TypeDB;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import static com.vaticle.typedb.client.api.TypeDBSession.Type.DATA;
import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.READ;
import static com.vaticle.typeql.lang.TypeQL.match;
import static com.vaticle.typeql.lang.TypeQL.var;

public class TypeDBClient implements Client<TypeDBSession, TypeDBTransaction> {

    private final com.vaticle.typedb.client.api.TypeDBClient nativeClient;
    private final ConcurrentHashMap<String, TypeDBSession> sessionMap;
    private final String database;

    private TypeDBClient(com.vaticle.typedb.client.api.TypeDBClient nativeClient, String database) {
        this.nativeClient = nativeClient;
        this.database = database;
        this.sessionMap = new ConcurrentHashMap<>();
    }

    public static TypeDBClient core(String hostUri, String database) {
        return new TypeDBClient(TypeDB.coreClient(hostUri), database);
    }

    public static TypeDBClient cluster(String hostUri, String database) {
        return new TypeDBClient(TypeDB.clusterClient(hostUri), database);
    }

    public com.vaticle.typedb.client.api.TypeDBClient unpack() {
        return nativeClient;
    }

    @Override
    public TypeDBSession session(Region region) {
        return sessionMap.computeIfAbsent(region.group(), k -> new TypeDBSession(nativeClient.session(database, DATA)));
    }

    @Override
    public String printStatistics() {
        StringBuilder str = new StringBuilder();
        try (com.vaticle.typedb.client.api.TypeDBSession session = nativeClient.session(database, DATA)) {
            try (com.vaticle.typedb.client.api.TypeDBTransaction tx = session.transaction(READ)) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                long numberOfEntities = tx.query().match(match(var("x").isa("entity")).count()).get().asLong();
                long numberOfAttributes = tx.query().match(match(var("x").isa("attribute")).count()).get().asLong();
                long numberOfRelations = tx.query().match(match(var("x").isa("relation")).count()).get().asLong();
                long numberOfThings = tx.query().match(match(var("x").isa("thing")).count()).get().asLong();

                str.append("Benchmark statistic:").append("\n");
                str.append("\n");
                str.append("Count 'entity': ").append(formatter.format(numberOfEntities)).append("\n");
                str.append("Count 'relation': ").append(formatter.format(numberOfRelations)).append("\n");
                str.append("Count 'attribute': ").append(formatter.format(numberOfAttributes)).append("\n");
                if (numberOfThings != numberOfEntities + numberOfAttributes + numberOfRelations) {
                    str.append("The sum of 'entity', 'relation', and 'attribute' counts do not match the total 'thing' count: ")
                            .append(formatter.format(numberOfThings)).append("\n");
                } else {
                    str.append("Count 'thing' (total): ").append(formatter.format(numberOfThings)).append("\n");
                }
                str.append("\n");
            }
        }
        return str.toString();
    }

    @Override
    public void closeSessions() {
        sessionMap.values().forEach(TypeDBSession::close);
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        nativeClient.close();
    }
}
