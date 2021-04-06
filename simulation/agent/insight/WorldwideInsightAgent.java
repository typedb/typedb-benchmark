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

package grakn.benchmark.simulation.agent.insight;

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.read.ReadAction;
import grakn.benchmark.simulation.agent.region.WorldAgent;
import grakn.benchmark.simulation.driver.DbDriver;
import grakn.benchmark.simulation.driver.DbOperation;
import grakn.benchmark.simulation.driver.DbOperationFactory;
import grakn.benchmark.simulation.world.World;

import java.util.Random;

/**
 * Agent to perform a single action, where that action requires no parameters
 *
 * @param <DB_OPERATION>
 */
public abstract class WorldwideInsightAgent<DB_OPERATION extends DbOperation> extends WorldAgent<DB_OPERATION> {
    public WorldwideInsightAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Regional getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new WorldWideWorker(iteration, tracker, random, test);
    }

    protected abstract ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation);

    public class WorldWideWorker extends WorldRegion {

        public WorldWideWorker(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World world) {
            for (int i = 0; i <= benchmarkContext.world().getScaleFactor(); i++) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
                    runAction(getAction(dbOperation));
                }
            }
        }
    }
}
