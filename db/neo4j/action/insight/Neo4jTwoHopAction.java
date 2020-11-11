package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.common.action.insight.TwoHopAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jTwoHopAction extends TwoHopAction<Neo4jOperation> {
    public Neo4jTwoHopAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(new Query(query()), "parent.email", null);
    }

    public static String query() {
        return "MATCH (city:City {locationName: \"London\"})<-[:BORN_IN]-(child:Person)-[:CHILD_OF]->(parent:Person)\n" +
                "RETURN parent.email";
    }
}
