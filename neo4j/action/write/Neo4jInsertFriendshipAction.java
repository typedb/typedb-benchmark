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

package grakn.benchmark.neo4j.action.write;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.write.InsertFriendshipAction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.EMAIL;
import static grakn.benchmark.neo4j.action.Model.START_DATE;

public class Neo4jInsertFriendshipAction extends InsertFriendshipAction<Neo4jTransaction, Record> {

    public Neo4jInsertFriendshipAction(Neo4jTransaction tx, LocalDateTime today, String friend1Email, String friend2Email) {
        super(tx, today, friend1Email, friend2Email);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("p1Email", friend1Email);
            put("p2Email", friend2Email);
            put("startDate", today);
        }};
        return Action.optionalSingleResult(tx.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH " +
                "(p1:Person),\n" +
                "(p2:Person)\n" +
                "WHERE p1.email = $p1Email AND p2.email = $p2Email AND NOT (p1)-[:FRIEND_OF]-(p2)\n" +
                "CREATE (p1)-[friendOf:FRIEND_OF {startDate: $startDate}]->(p2)\n" +
                "RETURN p1.email, p2.email, friendOf.startDate";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertFriendshipActionField.FRIEND1_EMAIL, answer.asMap().get("p1." + EMAIL));
                put(InsertFriendshipActionField.FRIEND2_EMAIL, answer.asMap().get("p2." + EMAIL));
                put(InsertFriendshipActionField.START_DATE, answer.asMap().get("friendOf." + START_DATE));
            }
        };
    }
}
