package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.ProductAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class ProductAgent extends Neo4jAgent<World.Continent> implements ProductAgentBase {

    @Override
    public void insertProduct(World.Continent continent, Double barcode, String productName, String productDescription) {
        String template = "" +
                "MATCH (continent:Continent {locationName: $continentName})\n" +
                "CREATE (product:Product {\n" +
                "   barcode: $barcode,\n" +
                "   name: $productName,\n" +
                "   description: $description\n" +
                "})-[:PRODUCED_IN]->(continent)";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("continentName", continent.name());
                put("barcode", barcode);
                put("productName", productName);
                put("description", productDescription);
        }};
        Query insertProductQuery = new Query(template, parameters);
        tx().execute(insertProductQuery);
    }

    static Query getProductsInContinentQuery(World.Continent continent) {
        String template = "" +
                "MATCH (continent:Continent {locationName: $continentName}),\n" +
                "(product:Product)-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("continentName", continent.name());
        }};
        return new Query(template, parameters);
    }
}
