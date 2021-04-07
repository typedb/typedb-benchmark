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
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.action.write.InsertParentShipAction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.EMAIL;

public class Neo4jInsertParentShipAction extends InsertParentShipAction<Neo4jTransaction, Record> {
    public Neo4jInsertParentShipAction(Neo4jTransaction tx, HashMap<SpouseType, String> marriage, String childEmail) {
        super(tx, marriage, childEmail);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("motherEmail", marriage.get(SpouseType.WIFE));
            put("fatherEmail", marriage.get(SpouseType.HUSBAND));
            put("childEmail", childEmail);
        }};
        return Action.singleResult(tx.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)\n" +
                "RETURN mother.email, father.email, child.email";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertParentShipActionField.WIFE_EMAIL, answer.asMap().get("mother." + EMAIL));
                put(InsertParentShipActionField.HUSBAND_EMAIL, answer.asMap().get("father." + EMAIL));
                put(InsertParentShipActionField.CHILD_EMAIL, answer.asMap().get("child." + EMAIL));
            }
        };
    }

    public enum InsertParentShipActionField implements ComparableField {
        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
    }
}
