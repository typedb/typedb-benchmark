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

package grakn.benchmark.simulation.agent.read;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.read.ReadAction;
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.common.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class ReadAgent<TX extends Transaction> extends Agent<World, TX> {

    public ReadAgent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    protected abstract ReadAction<TX, ?> getAction(TX tx);

    @Override
    protected List<World> getRegions(World world) {
        return Collections.singletonList(world);
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        for (int i = 0; i <= context.world().getScaleFactor(); i++) {
            try (TX tx = session.transaction(region.tracker(), context.iteration(), isTracing())) {
                runAction(getAction(tx), reports);
            }
        }
        return reports;
    }
}
