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

package grakn.benchmark.common.agent.insight;

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.action.read.ReadAction;
import grakn.benchmark.common.agent.base.SimulationContext;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;

public class FindCurrentResidentsAgent<DB_OPERATION extends DbOperation> extends WorldwideInsightAgent<DB_OPERATION> {


    public FindCurrentResidentsAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, SimulationContext simulationContext) {
        super(dbDriver, actionFactory, simulationContext);
    }

    @Override
    protected ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation) {
        return actionFactory().findCurrentResidentsAction(dbOperation);
    }
}
