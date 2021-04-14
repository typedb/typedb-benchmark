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

package grakn.benchmark.grakn.agent;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.MeanWageAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.pattern.variable.UnboundVariable;

import static grakn.benchmark.grakn.agent.Types.WAGE;
import static grakn.benchmark.grakn.agent.Types.WAGE_VALUE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknMeanWageAgent extends MeanWageAgent<GraknTransaction> {

    public GraknMeanWageAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchMeanWage(GraknTransaction tx) {
        UnboundVariable wageValue = var(WAGE_VALUE);
        tx.execute(match(var(WAGE).isa(WAGE).has(WAGE_VALUE, wageValue)).get(wageValue).mean(wageValue)).asDouble();
    }

    //    public static GraqlGet.Aggregate query() {
//        Statement wageValue = Graql.var(WAGE_VALUE);
//        return Graql.match(
//                Graql.var(WAGE).isa(WAGE)
//                        .has(WAGE_VALUE, wageValue)
//        ).get(wageValue.var()).mean(wageValue.var());
//    }
}
