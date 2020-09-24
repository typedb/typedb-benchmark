package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static java.util.Collections.shuffle;

public interface TransactionAgentBase extends InteractionAgent<World.Continent> {

    int NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE = 1;

    @Override
    default AgentResultSet iterate(Agent<World.Continent, ?> agent, World.Continent continent, SimulationContext simulationContext) {
        List<Long> companyNumbers;
        agent.newAction("getCompanyNumbersInContinent");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            companyNumbers = getCompanyNumbersInContinent(continent);
        }
        List<Double> productBarcodes;
        agent.newAction("getProductBarcodesInContinent");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            productBarcodes = getProductBarcodesInContinent(continent);
        }
        shuffle(companyNumbers, agent.random());

        int numTransactions = NUM_TRANSACTIONS_PER_COMPANY_ON_AVERAGE * simulationContext.world().getScaleFactor() * companyNumbers.size();
        // Company numbers is the list of sellers
        // Company numbers picked randomly is the list of buyers
        // Products randomly picked

        // See if we can allocate with a Pair, which is the buyer and the product id
        List<Pair<Long, Double>> transactions = new ArrayList<>();
        for (int i = 0; i < numTransactions; i++) {
            Long companyNumber = agent.pickOne(companyNumbers);
            Double productBarcode = agent.pickOne(productBarcodes);
            Pair<Long, Double> buyerAndProduct = new Pair(companyNumber, productBarcode);
            transactions.add(buyerAndProduct);
        }
        Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
            double value = agent.randomAttributeGenerator().boundRandomDouble(0.01, 10000.00);
            int productQuantity = agent.randomAttributeGenerator().boundRandomInt(1, 1000);
            boolean isTaxable = agent.randomAttributeGenerator().bool();
            agent.newAction("insertTransaction");
            try (ThreadTrace trace = traceOnThread(agent.action())) {
                insertTransaction(continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
            }
        });
        agent.commitAction();
        return null;
    }

    List<Long> getCompanyNumbersInContinent(World.Continent continent);

    List<Double> getProductBarcodesInContinent(World.Continent continent);

    void insertTransaction(World.Continent continent, Pair<Long, Double> transaction, long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);
}
