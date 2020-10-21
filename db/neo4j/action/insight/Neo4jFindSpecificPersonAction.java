package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

public class Neo4jFindSpecificPersonAction extends FindSpecificPersonAction<Neo4jOperation> {
    public Neo4jFindSpecificPersonAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        String query = "" +
                "MATCH (person:Person {email: " + PERSON_EMAIL_FOR_QUERY + "})" +
                "RETURN person.email";
        Record result = singleResult(dbOperation.execute(new Query(query)));
        return result.get("person.email").asString();
    }
}
