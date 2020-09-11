package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.neo4j.common.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.CompanyAgent.getCompanyNumbersInContinentQuery;
import static grakn.simulation.db.neo4j.agents.interaction.ProductAgent.getProductsInContinentQuery;

public class TransactionAgent extends grakn.simulation.db.common.agents.interaction.TransactionAgent<Neo4jContext> {
    @Override
    protected List<Long> getCompanyNumbersInContinent() {
        Query companiesQuery = getCompanyNumbersInContinentQuery(continent());
        log().query("getCompanyNumbersInContinent", companiesQuery);
        return ((Neo4jDriverWrapper.Session.Transaction) tx()).getOrderedAttribute(companiesQuery, "company.companyNumber", null);
    }

    @Override
    protected List<Double> getProductBarcodesInContinent() {
        Query productsQuery = getProductsInContinentQuery(continent());
        log().query("getProductBarcodesInContinent", productsQuery);
        return ((Neo4jDriverWrapper.Session.Transaction) tx()).getOrderedAttribute(productsQuery, "product.barcode", null);
    }

    @Override
    protected void insertTransaction(Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable) {
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
                put("continentName", continent().name());
                put("value", value);
                put("productQuantity", productQuantity);
                put("isTaxable", isTaxable);
        }};
        Query insertTransactionQuery = new Query(template, parameters);
        log().query("insertTransaction", insertTransactionQuery);
        ((Neo4jDriverWrapper.Session.Transaction) tx()).execute(insertTransactionQuery);
    }

    @Override
    protected int checkCount() {
        return 0;
    }
}
