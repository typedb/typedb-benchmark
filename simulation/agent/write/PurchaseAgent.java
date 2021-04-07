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

package grakn.benchmark.simulation.agent.write;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.read.CompaniesInCountryAction;
import grakn.benchmark.simulation.action.read.ProductsInContinentAction;
import grakn.benchmark.simulation.agent.base.Allocation;
import grakn.benchmark.simulation.agent.base.RandomValueGenerator;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CountryAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;
import grakn.common.collection.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static grakn.common.collection.Collections.pair;
import static java.util.Collections.shuffle;

public class PurchaseAgent<TX extends Transaction> extends CountryAgent<TX> {

    public PurchaseAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(dbDriver, actionFactory, context);
    }

    @Override
    protected void run(Session<TX> session, World.Country region, List<Action<?, ?>.Report> reports, Random random) {
        List<Long> companyNumbers;

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            CompaniesInCountryAction<TX> companiesInContinentAction = actionFactory().companiesInCountryAction(dbOperation, region, 100);
            companyNumbers = runAction(companiesInContinentAction, context.isTest(), reports);
        }
        shuffle(companyNumbers, random);

        List<Long> productBarcodes;
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            ProductsInContinentAction<?> productsInContinentAction = actionFactory().productsInContinentAction(dbOperation, region.continent());
            productBarcodes = runAction(productsInContinentAction, context.isTest(), reports);
        }

        int numTransactions = context.world().getScaleFactor() * companyNumbers.size();
        // Company numbers is the list of sellers
        // Company numbers picked randomly is the list of buyers
        // Products randomly picked

        // See if we can allocate with a Pair, which is the buyer and the product id
        List<Pair<Long, Long>> transactions = new ArrayList<>();
        for (int i = 0; i < numTransactions; i++) {
            Long companyNumber = pickOne(companyNumbers, random);
            Long productBarcode = pickOne(productBarcodes, random);
            Pair<Long, Long> buyerAndProduct = pair(companyNumber, productBarcode);
            transactions.add(buyerAndProduct);
        }
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
                double value = RandomValueGenerator.of(random).boundRandomDouble(0.01, 10000.00);
                int productQuantity = RandomValueGenerator.of(random).boundRandomInt(1, 1000);
                boolean isTaxable = RandomValueGenerator.of(random).bool();
                runAction((Action<?, ?>) actionFactory().insertTransactionAction(dbOperation, region, transaction, sellerCompanyNumber, value, productQuantity, isTaxable), context.isTest(), reports);
            });
            dbOperation.commit();
        }
    }
}
