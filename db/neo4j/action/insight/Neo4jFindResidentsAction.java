package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.FindResidentsAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jFindResidentsAction extends FindResidentsAction<Neo4jOperation> {
    public Neo4jFindResidentsAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.getOrderedAttribute(new Query(query()), "person.email", null);
    }

    public static String query() {
        return "MATCH (person:Person)-[residentOf:RESIDENT_OF]->(city:City {locationName: \"Berlin\"})\n" +
                "RETURN person.email";
    }
}
