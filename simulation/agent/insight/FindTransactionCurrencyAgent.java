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
import grakn.benchmark.simulation.driver.DbDriver;
import grakn.benchmark.simulation.driver.DbOperation;

public class FindTransactionCurrencyAgent<DB_OPERATION extends DbOperation> extends WorldwideInsightAgent<DB_OPERATION> {
    public FindTransactionCurrencyAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation) {
        return actionFactory().findTransactionCurrencyAction(dbOperation);
    }
}
