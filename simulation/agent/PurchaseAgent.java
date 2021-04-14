/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.simulation.agent;

import grakn.benchmark.simulation.common.Allocation;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.RandomValueGenerator;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.common.collection.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static grakn.common.collection.Collections.pair;
import static java.util.Collections.shuffle;

public abstract class PurchaseAgent<TX extends Transaction> extends Agent<GeoData.Country, TX> {

    public PurchaseAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.Country> regions() {
        return context.geoData().countries();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.Country region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        List<Long> companyNumbers;

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            companyNumbers = matchCompaniesInCountry(tx, region, 100);
        }
        shuffle(companyNumbers, random);

        List<Long> productBarcodes;
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            productBarcodes = matchProductsInContinent(tx, region.continent());
        }

        int numTransactions = context.scaleFactor() * companyNumbers.size();
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
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            Allocation.allocate(transactions, companyNumbers, (transaction, sellerCompanyNumber) -> {
                double value = RandomValueGenerator.of(random).boundRandomDouble(0.01, 10000.00);
                int productQuantity = RandomValueGenerator.of(random).boundRandomInt(1, 1000);
                boolean isTaxable = RandomValueGenerator.of(random).bool();
                insertPurchase(tx, region, transaction, sellerCompanyNumber, value, productQuantity, isTaxable);
            });
            tx.commit();
        }

        return reports;
    }

    protected abstract List<Long> matchCompaniesInCountry(TX tx, GeoData.Country region, int numCompanies);

    protected abstract List<Long> matchProductsInContinent(TX tx, GeoData.Continent continent);

    protected abstract void insertPurchase(TX tx, GeoData.Country region, Pair<Long, Long> transaction, Long sellerCompanyNumber, double value, int productQuantity, boolean isTaxable);
}
