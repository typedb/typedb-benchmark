package grakn.simulation.db.neo4j.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.SpouseType;
import grakn.simulation.common.action.write.InsertParentShipAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;

public class Neo4jInsertParentShipAction extends InsertParentShipAction<Neo4jOperation, Record> {
    public Neo4jInsertParentShipAction(Neo4jOperation dbOperation, HashMap<SpouseType, String> marriage, String childEmail) {
        super(dbOperation, marriage, childEmail);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("motherEmail", marriage.get(SpouseType.WIFE));
            put("fatherEmail", marriage.get(SpouseType.HUSBAND));
            put("childEmail", childEmail);
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)\n" +
                "RETURN mother.email, father.email, child.email";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object> () {
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
