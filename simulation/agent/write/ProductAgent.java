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

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.region.ContinentAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.util.Random;

public class ProductAgent<DB_OPERATION extends Transaction> extends ContinentAgent<DB_OPERATION> {

    public ProductAgent(Client<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Regional getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new RegionalProductAgent(iteration, tracker, random, test);
    }

    public class RegionalProductAgent extends ContinentRegion {
        public RegionalProductAgent(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<DB_OPERATION> dbOperationFactory, World.Continent continent) {
            int numProducts = benchmarkContext.world().getScaleFactor();
            try (DB_OPERATION dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                for (int i = 0; i < numProducts; i++) {
                    String productName = randomAttributeGenerator().boundRandomLengthRandomString(5, 20);
                    String productDescription = randomAttributeGenerator().boundRandomLengthRandomString(75, 100);
                    Long barcode = (long) uniqueId(benchmarkContext, i).hashCode();
                    runAction(actionFactory().insertProductAction(dbOperation, continent, barcode, productName, productDescription));
                }
                dbOperation.save();
            }
        }
    }
}
