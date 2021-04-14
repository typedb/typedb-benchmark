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

package grakn.benchmark.neo4j.agent;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jFriendshipAgent extends FriendshipAgent<Neo4jTransaction> {

    public Neo4jFriendshipAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(Neo4jTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return Neo4jMatcher.matchResidentsInCity(tx, city, numResidents, earliestDate);
    }

    @Override
    protected void insertFriendship(Neo4jTransaction tx, LocalDateTime today, String email1, String email2) {
        String query = "MATCH " +
                "(p1:Person),\n" +
                "(p2:Person)\n" +
                "WHERE p1.email = $p1Email AND p2.email = $p2Email AND NOT (p1)-[:FRIEND_OF]-(p2)\n" +
                "CREATE (p1)-[friendOf:FRIEND_OF {startDate: $startDate}]->(p2)\n" +
                "RETURN p1.email, p2.email, friendOf.startDate";
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("p1Email", email1);
            put("p2Email", email2);
            put("startDate", today);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<ComparableField, Object>() {
//            {
//                put(InsertFriendshipActionField.FRIEND1_EMAIL, answer.asMap().get("p1." + EMAIL));
//                put(InsertFriendshipActionField.FRIEND2_EMAIL, answer.asMap().get("p2." + EMAIL));
//                put(InsertFriendshipActionField.START_DATE, answer.asMap().get("friendOf." + START_DATE));
//            }
//        };
//    }
}
