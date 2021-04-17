/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.neo4j.driver;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.common.concept.Region;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.benchmark.simulation.driver.Client.TracingLabel.OPEN_SESSION;

public class Neo4jClient implements Client<Neo4jSession, Neo4jTransaction> {

    private final Driver nativeDriver;
    private final ConcurrentHashMap<String, Neo4jSession> sessionMap = new ConcurrentHashMap<>();

    public Neo4jClient(String hostUri) {
        this.nativeDriver = GraphDatabase.driver(hostUri, AuthTokens.basic("neo4j", "admin"));
    }

    public Driver unpack() {
        return nativeDriver;
    }

    @Override
    public Neo4jSession session(Region region) {
        return sessionMap.computeIfAbsent(region.name(), k -> {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(OPEN_SESSION.getName())) {
                return new Neo4jSession(nativeDriver.session());
            }
        });
    }

    @Override
    public String printStatistics() {
        StringBuilder str = new StringBuilder();
        try (org.neo4j.driver.Session nativeSession = nativeDriver.session()) {
            DecimalFormat formatter = new DecimalFormat("#,###");

            String numberOfNodesQ = "MATCH (n)\n RETURN count(n)";
            List<Record> numberOfNodesList = nativeSession.writeTransaction(tx -> {
                Result result = tx.run(new Query(numberOfNodesQ));
                return result.list();
            });
            long numberOfNodes = (long) numberOfNodesList.get(0).asMap().get("count(n)");

            String numberOfRelationshipsQ = "MATCH ()-->()\n RETURN count(*)";
            List<Record> numberOfRelationshipsList = nativeSession.writeTransaction(tx -> {
                Result result = tx.run(new Query(numberOfRelationshipsQ));
                return result.list();
            });
            long numberOfRelationships = (long) numberOfRelationshipsList.get(0).asMap().get("count(*)");

            str.append("Benchmark statistic:");
            str.append("\n");

            str.append("Count 'node': {}").append(formatter.format(numberOfNodes));
            str.append("Count 'relationship': {}").append(formatter.format(numberOfRelationships));
            str.append("\n");
        }
        return str.toString();
    }

    @Override
    public void closeSessions() {
        sessionMap.values().forEach(Neo4jSession::close);
        sessionMap.clear();
    }

    @Override
    public void close() {
        closeSessions();
        nativeDriver.close();
    }
}
