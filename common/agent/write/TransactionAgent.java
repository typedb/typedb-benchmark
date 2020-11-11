package grakn.simulation.common.agent.write;

import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.read.CompaniesInCountryAction;
import grakn.simulation.common.action.read.ProductsInContinentAction;
import grakn.simulation.common.agent.base.Allocation;
import grakn.simulation.common.agent.base.SimulationContext;
import grakn.simulation.common.agent.region.CountryAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.utils.Pair;
import grakn.simulation.common.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionAgent<DB_OPERATION extends DbOperation> extends CountryAgent<DB_OPERATION> {

    public TransactionAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Country country, SimulationContext simulationContext) {
            List<Long> companyNumbers;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                CompaniesInCountryAction<DB_OPERATION> companiesInContinentAction = actionFactory().companiesInCountryAction(dbOperation, country, 100);
                companyNumbers = runAction(companiesInContinentAction);
            }
            shuffle(companyNumbers);

            List<Double> productBarcodes;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                ProductsInContinentAction<?> productsInContinentAction = actionFactory().productsInContinentAction(dbOperation, country.continent());
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
                    runAction(actionFactory().insertTransactionAction(dbOperation, country, transaction, sellerCompanyNumber, value, productQuantity, isTaxable));
                });
                dbOperation.save();
            }
        }
    }
}
