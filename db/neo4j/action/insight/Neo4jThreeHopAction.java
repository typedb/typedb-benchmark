package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.ThreeHopAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jThreeHopAction extends ThreeHopAction<Neo4jOperation> {
    public Neo4jThreeHopAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        String query = "" +
                "MATCH (city:City {locationName: \"London\"})<-[:BORN_IN]-(child:Person)-[:CHILD_OF]->(parent:Person)\n" +
                "<-[:EMPLOYS]-(company:Company)" +
                "RETURN company.companyName";
        return dbOperation.getOrderedAttribute(new Query(query), "company.companyName", null);
    }
}
