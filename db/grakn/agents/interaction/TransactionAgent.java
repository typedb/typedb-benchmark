package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.utils.Pair;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.CompanyAgent.getCompanyNumbersInContinentQuery;
import static grakn.simulation.db.grakn.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.db.grakn.agents.interaction.ProductAgent.getProductsInContinentQuery;

public class TransactionAgent extends grakn.simulation.db.common.agents.interaction.TransactionAgent {

    @Override
    protected List<Long> getCompanyNumbersInContinent(){
        GraqlGet companiesQuery = getCompanyNumbersInContinentQuery(continent());
        log().query("getCompanyNumbersInCountry", companiesQuery);
        return getOrderedAttribute(tx().forGrakn(), companiesQuery, "company-number");
    }

    @Override
    protected List<Double> getProductBarcodesInContinent() {
        GraqlGet productsQuery = getProductsInContinentQuery(continent());
        log().query("getProductBarcodesInContinent", productsQuery);
        return getOrderedAttribute(tx().forGrakn(), productsQuery, "product-barcode");
    }

    @Override
    protected void insertTransaction(Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable){
        GraqlInsert insertTransactionQuery = Graql.match(
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", transaction.getSecond()),
                Graql.var("c-buyer").isa("company")
                        .has("company-number", transaction.getFirst()),
                Graql.var("c-seller").isa("company")
                        .has("company-number", sellerCompanyNumber),
                Graql.var("continent").isa("continent")
                        .has("location-name", continent().name()))
                .insert(
                        Graql.var("transaction")
                                .isa("transaction")
                                .rel("transaction_seller", Graql.var("c-seller"))
                                .rel("transaction_buyer", Graql.var("c-buyer"))
                                .rel("transaction_merchandise", Graql.var("product"))
//                                .has("currency")  // TODO Add currency https://github.com/graknlabs/simulation/issues/31
                                .has("value", value)
                                .has("product-quantity", productQuantity)
                                .has("is-taxable", isTaxable),
                        Graql.var("locates")
                                .isa("locates")
                                .rel("locates_location", Graql.var("country"))
                                .rel("locates_located", Graql.var("transaction"))

                );
        log().query("insertTransaction", insertTransactionQuery);
        tx().forGrakn().execute(insertTransactionQuery);
    }
}
