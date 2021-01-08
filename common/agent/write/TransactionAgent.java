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

package grakn.benchmark.common.agent.write;

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.action.read.CompaniesInCountryAction;
import grakn.benchmark.common.action.read.ProductsInContinentAction;
import grakn.benchmark.common.agent.base.Allocation;
import grakn.benchmark.common.agent.region.CountryAgent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.world.World;
import grakn.common.collection.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static grakn.common.collection.Collections.pair;

public class TransactionAgent<DB_OPERATION extends DbOperation> extends CountryAgent<DB_OPERATION> {

    public TransactionAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.common.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Region getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new Country(iteration, tracker, random, test);
    }

    public class Country extends CountryRegion {
        public Country(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Country country) {
            List<Long> companyNumbers;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                CompaniesInCountryAction<DB_OPERATION> companiesInContinentAction = actionFactory().companiesInCountryAction(dbOperation, country, 100);
                companyNumbers = runAction(companiesInContinentAction);
            }
            shuffle(companyNumbers);

            List<Long> productBarcodes;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                ProductsInContinentAction<?> productsInContinentAction = actionFactory().productsInContinentAction(dbOperation, country.continent());
                productBarcodes = runAction(productsInContinentAction);
            }

            int numTransactions = benchmarkContext.world().getScaleFactor() * companyNumbers.size();
            // Company numbers is the list of sellers
            // Company numbers picked randomly is the list of buyers
            // Products randomly picked

            // See if we can allocate with a Pair, which is the buyer and the product id
            List<Pair<Long, Long>> transactions = new ArrayList<>();
            for (int i = 0; i < numTransactions; i++) {
                Long companyNumber = pickOne(companyNumbers);
                Long productBarcode = pickOne(productBarcodes);
                Pair<Long, Long> buyerAndProduct = pair(companyNumber, productBarcode);
                transactions.add(buyerAndProduct);
            }
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
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
