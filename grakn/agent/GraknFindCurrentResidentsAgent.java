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
import grakn.benchmark.simulation.agent.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;

import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.IS_CURRENT;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY_LOCATION;
import static grakn.benchmark.grakn.agent.Types.RESIDENCY_RESIDENT;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknFindCurrentResidentsAgent extends FindCurrentResidentsAgent<GraknTransaction> {

    public GraknFindCurrentResidentsAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchCurrentResidents(GraknTransaction tx) {
        tx.sortedExecute(match(
                var(CITY).isa(CITY)
                        .has(LOCATION_NAME, "Berlin"),
                var(RESIDENCY)
                        .rel(RESIDENCY_LOCATION, var(CITY))
                        .rel(RESIDENCY_RESIDENT, var(PERSON))
                        .isa(RESIDENCY)
                        .has(IS_CURRENT, true),
                var(PERSON).isa(PERSON).has(EMAIL, var(EMAIL))
        ), EMAIL, null);
    }

}
