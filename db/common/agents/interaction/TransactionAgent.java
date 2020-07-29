package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.ContinentAgent;

import java.util.ArrayList;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class TransactionAgent extends ContinentAgent {

    private int NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE = 1;

    @Override
    public final void iterate() {
        List<Long> companyNumbers;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getCompanyNumbersInContinent"))) {
            companyNumbers = getCompanyNumbersInContinent();
        }
        List<Double> productBarcodes;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getProductBarcodesInContinent"))) {
            productBarcodes = getProductBarcodesInContinent();
        }
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
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertTransaction"))) {
                insertTransaction(transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
            }
        });
        commitTxWithTracing();
    }

    abstract protected List<Long> getCompanyNumbersInContinent();

    abstract protected List<Double> getProductBarcodesInContinent();

    abstract protected void insertTransaction(Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(0, world().getScaleFactor());
    }
}
