package grakn.simulation.neo4j.action.read;

import grakn.simulation.common.action.read.ProductsInContinentAction;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jProductsInContinentAction extends ProductsInContinentAction<Neo4jOperation> {

    public Neo4jProductsInContinentAction(Neo4jOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Double> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("continentName", continent.name());
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "product.barcode", null);
    }

    public static String query() {
        return "MATCH (continent:Continent {locationName: $continentName}),\n" +
                "(product:Product)-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode";
    }
}
