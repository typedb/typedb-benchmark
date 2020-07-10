package grakn.simulation.agents;

import grakn.simulation.agents.common.CountryAgent;
import grakn.simulation.common.Allocation;
import grakn.simulation.common.Pair;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.ArrayList;
import java.util.List;

import static grakn.simulation.agents.CompanyAgent.getCompanyNumbersInCountryQuery;
import static grakn.simulation.agents.ProductAgent.getProductsInContinentQuery;
import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;

public class TransactionAgent extends CountryAgent {

    private int NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE = 1;

    @Override
    public void iterate() {
        List<Long> companyNumbers = getCompanyNumbersInCountry();
        List<Double> productBarcodes = getProductBarcodesInContinent();
        shuffle(companyNumbers);

        int numTransactions = NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE * world().getScaleFactor() * companyNumbers.size();

        // Company numbers is the list of sellers
        // Company numbers picked randomly is the list of buyers
        // Products randomly picked

        // See if we can allocate with a Pair, which is the buyer and the product id
        List<Pair<Long, Double>> transactions = new ArrayList<>();

        for (int i = 0; i < numTransactions; i++) {
            Long companyNumber = pickOne(companyNumbers);
            Double productBarcode = pickOne(productBarcodes);
            Pair<Long, Double> buyerAndProduct = new Pair(companyNumber, productBarcode);
            transactions.add(buyerAndProduct);
        }
        Allocation.allocate(transactions, companyNumbers, this::insertTransaction);
        tx().commit();
    }

    private List<Long> getCompanyNumbersInCountry(){
        GraqlGet companiesQuery = getCompanyNumbersInCountryQuery(country());
        log().query("getCompanyNumbersInCountry", companiesQuery);
        return getOrderedAttribute(tx().forGrakn(), companiesQuery, "company-number");
    }

    private List<Double> getProductBarcodesInContinent() {
        GraqlGet.Unfiltered productsQuery = getProductsInContinentQuery(country().continent());
        log().query("getProductBarcodesInContinent", productsQuery);
        return getOrderedAttribute(tx().forGrakn(), productsQuery, "product-barcode");
    }

    private void insertTransaction(Pair<Long, Double> transaction, Long sellerCompanyNumber){
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
                                .has("value", randomAttributeGenerator().boundRandomDouble(0.01, 10000.00))
                                .has("product-quantity", randomAttributeGenerator().boundRandomInt(1, 1000))
                                .has("is-taxable", randomAttributeGenerator().bool()),
                        Graql.var("locates")
                                .isa("locates")
                                .rel("locates_location", Graql.var("country"))
                                .rel("locates_located", Graql.var("transaction"))

                );
        log().query("insertTransaction", insertTransactionQuery);
        tx().forGrakn().execute(insertTransactionQuery);
    }
}
