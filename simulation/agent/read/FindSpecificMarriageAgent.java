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

package grakn.benchmark.simulation.agent.read;

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.read.ReadAction;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Client;

public class FindSpecificMarriageAgent<TX extends Transaction> extends ReadAgent<TX> {

    public FindSpecificMarriageAgent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected ReadAction<TX, ?> getAction(TX tx) {
        return actionFactory().findSpecificMarriageAction(tx);
    }
}
