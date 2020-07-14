package grakn.simulation.grakn.agents;

import grakn.simulation.common.Pair;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;

import static grakn.simulation.grakn.agents.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.grakn.agents.CompanyAgent.getCompanyNumbersInCountryQuery;
import static grakn.simulation.grakn.agents.ProductAgent.getProductsInContinentQuery;

public class TransactionAgent extends grakn.simulation.agents.interaction.TransactionAgent {

    @Override
    protected List<Long> getCompanyNumbersInCountry(){
        GraqlGet companiesQuery = getCompanyNumbersInCountryQuery(country());
        log().query("getCompanyNumbersInCountry", companiesQuery);
        return getOrderedAttribute(tx().forGrakn(), companiesQuery, "company-number");
    }

    @Override
    protected List<Double> getProductBarcodesInContinent() {
        GraqlGet.Unfiltered productsQuery = getProductsInContinentQuery(country().continent());
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
                Graql.var("country").isa("country")
                        .has("location-name", country().name()))
                .insert(
                        Graql.var("transaction")
                                .isa("transaction")
                                .rel("transaction_vendor", Graql.var("c-seller"))
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
