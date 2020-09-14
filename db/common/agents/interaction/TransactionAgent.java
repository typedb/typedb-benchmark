package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.region.ContinentAgent;

import java.util.ArrayList;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface TransactionAgent extends InteractionAgent<World.Continent> {

    private int NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE = 1;

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        List<Long> companyNumbers;
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getCompanyNumbersInContinent"))) {
            companyNumbers = getCompanyNumbersInContinent();
        }
        List<Double> productBarcodes;
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getProductBarcodesInContinent"))) {
            productBarcodes = getProductBarcodesInContinent();
        }
        shuffle(companyNumbers, agent.random());

        int numTransactions = NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE * iterationContext.world().getScaleFactor() * companyNumbers.size();

        // Company numbers is the list of sellers
        // Company numbers picked randomly is the list of buyers
        // Products randomly picked

        // See if we can allocate with a Pair, which is the buyer and the product id
        List<Pair<Long, Double>> transactions = new ArrayList<>();
        agent.startAction();
        for (int i = 0; i < numTransactions; i++) {
            Long companyNumber = agent.pickOne(companyNumbers);
            Double productBarcode = agent.pickOne(productBarcodes);
            Pair<Long, Double> buyerAndProduct = new Pair(companyNumber, productBarcode);
            transactions.add(buyerAndProduct);
        }
        Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
            Double value = agent.randomAttributeGenerator().boundRandomDouble(0.01, 10000.00);
            Integer productQuantity = agent.randomAttributeGenerator().boundRandomInt(1, 1000);
            Boolean isTaxable = agent.randomAttributeGenerator().bool();
            try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertTransaction"))) {
                insertTransaction(transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
            }
        });
        agent.commitAction();
        return null;
    }

    abstract protected List<Long> getCompanyNumbersInContinent();

    abstract protected List<Double> getProductBarcodesInContinent();

    abstract protected void insertTransaction(Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);

}
