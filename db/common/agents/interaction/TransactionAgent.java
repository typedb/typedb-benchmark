package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.ContinentAgent;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionAgent<DB_DRIVER extends DbDriver> extends ContinentAgent<DB_DRIVER> {

    public TransactionAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalTransactionAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalTransactionAgent(simulationStep, tracker, random, test);
    }

    public class RegionalTransactionAgent extends RegionalAgent {
        public RegionalTransactionAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.Continent continent, SimulationContext simulationContext) {
            List<Long> companyNumbers;

            CompaniesInContinentAction companiesInContinentAction = dbOperationController.actionFactory().companiesInContinentAction(continent);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(companiesInContinentAction, tracker())) {
                companyNumbers = runAction(companiesInContinentAction);
            }
            shuffle(companyNumbers);

            List<Double> productBarcodes;
            ProductsInContinentAction productsInContinentAction = dbOperationController.actionFactory().productsInContinentAction(continent);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(productsInContinentAction, tracker())) {
                productBarcodes = runAction(productsInContinentAction);
            }

            int numTransactions = simulationContext.world().getScaleFactor() * companyNumbers.size();
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
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertTransaction", tracker())) {
                Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
                    double value = randomAttributeGenerator().boundRandomDouble(0.01, 10000.00);
                    int productQuantity = randomAttributeGenerator().boundRandomInt(1, 1000);
                    boolean isTaxable = randomAttributeGenerator().bool();
                    runAction(dbOperationController.actionFactory().insertTransactionAction(continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable));
                });
                dbOperation.save();
            }
        }
    }
}
