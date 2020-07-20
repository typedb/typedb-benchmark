package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.world.World;

import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class ProductAgent extends grakn.simulation.db.common.agents.interaction.ProductAgent {

    @Override
    protected void insertProduct(Double barcode, String productName, String productDescription) {
        String template = "" +
                "MATCH (continent:Continent {locationName: $continentName})\n" +
                "CREATE (product:Product {\n" +
                "   barcode: $barcode,\n" +
                "   name: $productName,\n" +
                "   description: $description\n" +
                "})-[:PRODUCED_IN]->(continent)";
        Object[] parameters = new Object[]{
                "continentName", continent().name(),
                "barcode", barcode,
                "productName", productName,
                "description", productDescription
        };
        Neo4jQuery insertProductQuery = new Neo4jQuery(template, parameters);
        log().query("insertProduct", insertProductQuery);
        run(tx().forNeo4j(), insertProductQuery);
    }

    static Neo4jQuery getProductsInContinentQuery(World.Continent continent) {
        String template = "" +
                "MATCH (continent:Continent {locationName: $continentName}),\n" +
                "(product:Product)-[:PRODUCED_IN]->(continent)\n" +
                "RETURN product.barcode";
        Object[] parameters = new Object[]{
                "continentName", continent.name(),
        };
        return new Neo4jQuery(template, parameters);
    }
}
