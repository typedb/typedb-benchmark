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

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.read.ReadAction;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.WorldAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.util.List;
import java.util.Random;

/**
 * Agent to perform a single action, where that action requires no parameters
 *
 * @param <TX>
 */
public abstract class WorldwideInsightAgent<TX extends Transaction> extends WorldAgent<TX> {
    public WorldwideInsightAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(World region, Random random, SimulationContext context) {
        return new WorldWideWorker(region, random, context);
    }

    protected abstract ReadAction<TX, ?> getAction(TX dbOperation);

    public class WorldWideWorker extends WorldRegion {

        public WorldWideWorker(World region, Random random, SimulationContext context) {
            super(region, random, context);
        }

        @Override
        protected void run(Session<TX> session, World region, List<Action<?, ?>.Report> reports, Random random) {
            for (int i = 0; i <= context.world().getScaleFactor(); i++) {
                try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
                    runAction((Action<?, ?>) getAction(dbOperation), context.isTest(), reports);
                }
            }
        }
    }
}
