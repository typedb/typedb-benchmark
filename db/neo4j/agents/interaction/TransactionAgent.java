package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.utils.Pair;

import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.CompanyAgent.getCompanyNumbersInContinentQuery;
import static grakn.simulation.db.neo4j.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.db.neo4j.agents.interaction.ProductAgent.getProductsInContinentQuery;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class TransactionAgent extends grakn.simulation.db.common.agents.interaction.TransactionAgent {
    @Override
    protected List<Long> getCompanyNumbersInContinent() {
        Neo4jQuery companiesQuery = getCompanyNumbersInContinentQuery(continent());
        log().query("getCompanyNumbersInContinent", companiesQuery);
        return getOrderedAttribute(tx(), companiesQuery, "company.companyNumber");
    }

    @Override
    protected List<Double> getProductBarcodesInContinent() {
        Neo4jQuery productsQuery = getProductsInContinentQuery(continent());
        log().query("getProductBarcodesInContinent", productsQuery);
        return getOrderedAttribute(tx(), productsQuery, "product.barcode");
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

        Object[] parameters = new Object[]{
                "barcode", transaction.getSecond(),
                "buyerNumber", transaction.getFirst(),
                "sellerNumber", sellerCompanyNumber,
                "continentName", continent().name(),
                "value", value,
                "productQuantity", productQuantity,
                "isTaxable", isTaxable
        };
        Neo4jQuery insertTransactionQuery = new Neo4jQuery(template, parameters);
        log().query("insertTransaction", insertTransactionQuery);
        run(tx(), insertTransactionQuery);
    }
}
