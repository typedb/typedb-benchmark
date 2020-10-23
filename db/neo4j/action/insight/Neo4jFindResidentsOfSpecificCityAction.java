package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.FindResidentsOfSpecificCityAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jFindResidentsOfSpecificCityAction extends FindResidentsOfSpecificCityAction<Neo4jOperation> {
    public Neo4jFindResidentsOfSpecificCityAction(Neo4jOperation dbOperation) {
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
