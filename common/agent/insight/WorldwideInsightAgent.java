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

package grakn.simulation.common.agent.insight;

import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.read.ReadAction;
import grakn.simulation.common.agent.base.SimulationContext;
import grakn.simulation.common.agent.region.WorldAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;

import java.util.Random;

/**
 * Agent to perform a single action, where that action requires no parameters
 * @param <DB_OPERATION>
 */
public abstract class WorldwideInsightAgent<DB_OPERATION extends DbOperation> extends WorldAgent<DB_OPERATION> {
    public WorldwideInsightAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new WorldWideWorker(simulationStep, tracker, random, test);
    }

    protected abstract ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation);

    public class WorldWideWorker extends RegionalAgent {

        public WorldWideWorker(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World world, SimulationContext simulationContext) {
            for (int i = 0; i <= simulationContext.world().getScaleFactor(); i++) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                    runAction(getAction(dbOperation));
                }
            }
        }
    }
}
