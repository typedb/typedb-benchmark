/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    protected Country getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new Country(simulationStep, tracker, random, test);
    }

    public class Country extends CountryRegion {
        public Country(int simulationStep, String tracker, Random random, boolean test) {
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

            List<Long> productBarcodes;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                ProductsInContinentAction<?> productsInContinentAction = actionFactory().productsInContinentAction(dbOperation, country.continent());
                productBarcodes = runAction(productsInContinentAction);
            }

            int numTransactions = simulationContext.world().getScaleFactor() * companyNumbers.size();
            // Company numbers is the list of sellers
            // Company numbers picked randomly is the list of buyers
            // Products randomly picked

            // See if we can allocate with a Pair, which is the buyer and the product id
            List<Pair<Long, Long>> transactions = new ArrayList<>();
            for (int i = 0; i < numTransactions; i++) {
                Long companyNumber = pickOne(companyNumbers);
                Long productBarcode = pickOne(productBarcodes);
                Pair<Long, Long> buyerAndProduct = new Pair<>(companyNumber, productBarcode);
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
