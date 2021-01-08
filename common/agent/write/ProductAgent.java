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
import grakn.benchmark.common.agent.region.ContinentAgent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.world.World;

import java.util.Random;

public class ProductAgent<DB_OPERATION extends DbOperation> extends ContinentAgent<DB_OPERATION> {

    public ProductAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.common.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Region getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new RegionalProductAgent(iteration, tracker, random, test);
    }

    public class RegionalProductAgent extends ContinentRegion {
        public RegionalProductAgent(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.Continent continent) {
            int numProducts = benchmarkContext.world().getScaleFactor();
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
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
