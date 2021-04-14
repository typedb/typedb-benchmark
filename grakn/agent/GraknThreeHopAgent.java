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
import grakn.benchmark.simulation.agent.ThreeHopAgent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;

import static grakn.benchmark.grakn.agent.Types.BORN_IN;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_CHILD;
import static grakn.benchmark.grakn.agent.Types.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.COMPANY;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NAME;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_EMPLOYEE;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_EMPLOYER;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_CHILD;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknThreeHopAgent extends ThreeHopAgent<GraknTransaction> {

    public GraknThreeHopAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void matchThreeHop(GraknTransaction tx) {
        tx.sortedExecute(match(
                var(CITY).isa(CITY).has(LOCATION_NAME, "London"),
                var().rel(BORN_IN_PLACE_OF_BIRTH, var(CITY)).rel(BORN_IN_CHILD, var("child")).isa(BORN_IN),
                var("child").isa(PERSON),
                var().rel(PARENTSHIP_PARENT, var("parent")).rel(PARENTSHIP_CHILD, var("child")).isa(PARENTSHIP),
                var("parent").isa(PERSON),
                var().rel(EMPLOYMENT_EMPLOYEE, var("parent")).rel(EMPLOYMENT_EMPLOYER, var(COMPANY)).isa(EMPLOYMENT),
                var(COMPANY).isa(COMPANY).has(COMPANY_NAME, var(COMPANY_NAME))
        ), COMPANY_NAME, null);
    }

}
