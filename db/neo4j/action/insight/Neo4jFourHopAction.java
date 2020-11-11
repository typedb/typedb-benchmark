package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.common.action.insight.FourHopAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jFourHopAction extends FourHopAction<Neo4jOperation> {
    public Neo4jFourHopAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(new Query(query()), "seller.companyName", null);
    }

    public static String query() {
        return "" +
                    "MATCH (city:City {locationName: \"London\"})<-[:BORN_IN]-(child:Person)-[:CHILD_OF]->(parent:Person)\n" +
                    "<-[:EMPLOYS]-(buyer:Company)<-[:BUYER]-(seller:Company)" +
                    "RETURN seller.companyName";
    }
}
