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
import grakn.benchmark.simulation.agent.base.RandomValueGenerator;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.ContinentAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.util.Random;

public class ProductAgent<TX extends Transaction> extends ContinentAgent<TX> {

    public ProductAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(int iteration, String tracker, Random random, boolean test) {
        return new RegionalProductAgent(iteration, tracker, random, test);
    }

    public class RegionalProductAgent extends ContinentRegion {
        public RegionalProductAgent(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<TX> session, World.Continent continent) {
            int numProducts = context.world().getScaleFactor();
            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                for (int i = 0; i < numProducts; i++) {
                    String productName = RandomValueGenerator.of(random()).boundRandomLengthRandomString(5, 20);
                    String productDescription = RandomValueGenerator.of(random()).boundRandomLengthRandomString(75, 100);
                    long barcode = uniqueId(context, tracker(), i).hashCode();
                    runAction((Action<?, ?>) actionFactory().insertProductAction(dbOperation, continent, barcode, productName, productDescription), isTest(), actionReports());
                }
                dbOperation.commit();
            }
        }
    }
}
