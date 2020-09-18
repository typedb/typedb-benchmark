package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.TransactionAgentBase;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.CompanyAgent.getCompanyNumbersInContinentQuery;
import static grakn.simulation.db.neo4j.agents.interaction.ProductAgent.getProductsInContinentQuery;

public class TransactionAgent extends Neo4jAgent<World.Continent> implements TransactionAgentBase {
    @Override
    public List<Long> getCompanyNumbersInContinent(World.Continent continent, String scope) {
        Query companiesQuery = getCompanyNumbersInContinentQuery(continent);
        log().query(this.tracker(), scope, companiesQuery);
        return tx().getOrderedAttribute(companiesQuery, "company.companyNumber", null);
    }

    @Override
    public List<Double> getProductBarcodesInContinent(World.Continent continent, String scope) {
        Query productsQuery = getProductsInContinentQuery(continent);
        log().query(this.tracker(), scope, productsQuery);
        return tx().getOrderedAttribute(productsQuery, "product.barcode", null);
    }

    @Override
    public void insertTransaction(World.Continent continent, Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
        String template = "" +
                "MATCH (product:Product {barcode: $barcode}),\n" +
                "(buyer:Company {companyNumber: $buyerNumber}),\n" +
                "(seller:Company {companyNumber: $sellerNumber}),\n" +
                "(continent:Continent {locationName: $continentName})\n" +
                "CREATE (transaction:Transaction{\n" +
                "   value: $value,\n" +
                "   productQuantity: $productQuantity,\n" +
                "   isTaxable: $isTaxable,\n" +
                "   locationName: continent.locationName\n" + // This could be a relation, but would be inconsistent with how location is represented elsewhere
                "}),\n" +
                "(transaction)-[:SELLER]->(seller)," +
                "(transaction)-[:BUYER]->(buyer)," +
                "(transaction)-[:MERCHANDISE]->(product)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("barcode", transaction.getSecond());
                put("buyerNumber", transaction.getFirst());
                put("sellerNumber", sellerCompanyNumber);
                put("continentName", continent.name());
                put("value", value);
                put("productQuantity", productQuantity);
                put("isTaxable", isTaxable);
        }};
        Query insertTransactionQuery = new Query(template, parameters);
        log().query(this.tracker(), "insertTransaction", insertTransactionQuery);
        tx().execute(insertTransactionQuery);
    }
}
