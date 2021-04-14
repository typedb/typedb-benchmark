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
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.pattern.variable.UnboundVariable;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.FRIENDSHIP;
import static grakn.benchmark.grakn.agent.Types.FRIENDSHIP_FRIEND;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.START_DATE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.not;
import static graql.lang.Graql.var;

public class GraknFriendshipAgent extends FriendshipAgent<GraknTransaction> {

    public GraknFriendshipAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(GraknTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return GraknMatcher.matchResidentsInCity(tx, city, numResidents, earliestDate);
    }

    @Override
    protected void insertFriendship(GraknTransaction tx, LocalDateTime today, String email1, String email2) {
        UnboundVariable p1 = var("p1");
        UnboundVariable p2 = var("p2");

        tx.execute(match(
                p1.isa(PERSON).has(EMAIL, email1),
                p2.isa(PERSON).has(EMAIL, email2),
                not(var().rel(FRIENDSHIP_FRIEND, p1).rel(FRIENDSHIP_FRIEND, p2).isa(FRIENDSHIP))
        ).insert(
                var(FRIENDSHIP).rel(FRIENDSHIP_FRIEND, p1).rel(FRIENDSHIP_FRIEND, p2).isa(FRIENDSHIP).has(START_DATE, today)
        ));
    }

    //    @Override
//    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<ComparableField, Object>() {
//            {
//                put(InsertFriendshipActionField.FRIEND1_EMAIL, tx.getOnlyAttributeOfThing(answer, "p1", EMAIL));
//                put(InsertFriendshipActionField.FRIEND2_EMAIL, tx.getOnlyAttributeOfThing(answer, "p2", EMAIL));
//                put(InsertFriendshipActionField.START_DATE, tx.getOnlyAttributeOfThing(answer, FRIENDSHIP, START_DATE));
//            }
//        };
//
//    }
}
