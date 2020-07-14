package grakn.simulation.agents.interaction;

import grakn.simulation.agents.world.CountryAgent;
import grakn.simulation.agents.utils.Allocation;
import grakn.simulation.agents.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class TransactionAgent extends CountryAgent {

    private int NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE = 1;

    @Override
    public final void iterate() {
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
        Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
            Double value = randomAttributeGenerator().boundRandomDouble(0.01, 10000.00);
            Integer productQuantity = randomAttributeGenerator().boundRandomInt(1, 1000);
            Boolean isTaxable = randomAttributeGenerator().bool();
            insertTransaction(transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
        });
        tx().commit();
    }

    abstract protected List<Long> getCompanyNumbersInCountry();

    abstract protected List<Double> getProductBarcodesInContinent();

    abstract protected void insertTransaction(Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);
}
