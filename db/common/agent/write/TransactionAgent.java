package grakn.simulation.db.common.agent.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.CompaniesInContinentAction;
import grakn.simulation.db.common.action.read.ProductsInContinentAction;
import grakn.simulation.db.common.agent.region.ContinentAgent;
import grakn.simulation.db.common.agent.base.Allocation;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends ContinentAgent<DB_DRIVER, DB_OPERATION> {

    public TransactionAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Continent continent, SimulationContext simulationContext) {
            List<Long> companyNumbers;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                CompaniesInContinentAction<?> companiesInContinentAction = actionFactory().companiesInContinentAction(dbOperation, continent);
                companyNumbers = runAction(companiesInContinentAction);
            }
            shuffle(companyNumbers);

            List<Double> productBarcodes;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                ProductsInContinentAction<?> productsInContinentAction = actionFactory().productsInContinentAction(dbOperation, continent);
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
                Pair<Long, Double> buyerAndProduct = new Pair<>(companyNumber, productBarcode);
                transactions.add(buyerAndProduct);
            }
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
                    double value = randomAttributeGenerator().boundRandomDouble(0.01, 10000.00);
                    int productQuantity = randomAttributeGenerator().boundRandomInt(1, 1000);
                    boolean isTaxable = randomAttributeGenerator().bool();
                    runAction(actionFactory().insertTransactionAction(dbOperation, continent, transaction, sellerCompanyNumber, value, productQuantity, isTaxable));
                });
                dbOperation.save();
            }
        }
    }
}
