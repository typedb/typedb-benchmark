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
import grakn.benchmark.simulation.agent.TwoHopAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;

import static grakn.benchmark.grakn.agent.Types.BORN_IN;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_CHILD;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_CHILD;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknTwoHopAgent extends TwoHopAgent<GraknTransaction> {

    public GraknTwoHopAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchTwoHop(GraknTransaction tx) {
        tx.sortedExecute(match(
                var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                var().rel(BORN_IN_PLACE_OF_BIRTH, var(CITY)).rel(BORN_IN_CHILD, var("child")).isa(BORN_IN),
                var("child").isa(PERSON),
                var().rel(PARENTSHIP_PARENT, var("parent")).rel(PARENTSHIP_CHILD, var("child")).isa(PARENTSHIP),
                var("parent").isa(PERSON).has(EMAIL, var(EMAIL))
        ), EMAIL, null);
    }
}
