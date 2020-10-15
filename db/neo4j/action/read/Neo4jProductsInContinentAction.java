package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.ProductsInContinentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jProductsInContinentAction extends ProductsInContinentAction<Neo4jOperation> {

    public Neo4jProductsInContinentAction(Neo4jOperation dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Double> run() {
        String template = "" +
                "MATCH (continent:Continent {locationName: $continentName}),\n" +
                "(product:Product)-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("continentName", continent.name());
        }};
        return dbOperation.getOrderedAttribute(new Query(template, parameters), "product.barcode", null);
    }
}
