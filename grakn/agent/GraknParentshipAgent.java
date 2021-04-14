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
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.agent.ParentshipAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.LOCATES;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATED;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATION;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_HUSBAND;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_ID;
import static grakn.benchmark.grakn.agent.Types.MARRIAGE_WIFE;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_CHILD;
import static grakn.benchmark.grakn.agent.Types.PARENTSHIP_PARENT;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.not;
import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;

public class GraknParentshipAgent extends ParentshipAgent<GraknTransaction> {

    public GraknParentshipAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchBirthsInCity(GraknTransaction tx, GeoData.City city, LocalDateTime today) {
        return GraknMatcher.matchBirthsInCity(tx, city, today);
    }

    @Override
    protected List<HashMap<MarriageAgent.SpouseType, String>> matchMarriedCouple(GraknTransaction tx, GeoData.City city) {
        return GraknMatcher.matchMarriedCouple(tx, city);
    }

    @Override
    protected void insertParentship(GraknTransaction tx, HashMap<MarriageAgent.SpouseType, String> marriage, String childEmail) {
        // Parentship where parents have multiple children is represented as multiple ternary relations, each with
        // both parents and one child. They had these children at the same time, and will not have any subsequently.
        UnboundVariable parentship = var(PARENTSHIP);
        UnboundVariable child = var("child");
        UnboundVariable mother = var("mother");
        UnboundVariable father = var("father");

        tx.execute(match(
                mother.isa(PERSON).has(EMAIL, marriage.get(MarriageAgent.SpouseType.WIFE)),
                father.isa(PERSON).has(EMAIL, marriage.get(MarriageAgent.SpouseType.HUSBAND)),
                child.isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                parentship.rel(PARENTSHIP_PARENT, father).rel(PARENTSHIP_PARENT, mother).rel(PARENTSHIP_CHILD, child).isa(PARENTSHIP)
        ));

    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<ComparableField, Object>() {
//            {
//                put(InsertParentShipActionField.WIFE_EMAIL, tx.getOnlyAttributeOfThing(answer, "mother", EMAIL));
//                put(InsertParentShipActionField.HUSBAND_EMAIL, tx.getOnlyAttributeOfThing(answer, "father", EMAIL));
//                put(InsertParentShipActionField.CHILD_EMAIL, tx.getOnlyAttributeOfThing(answer, "child", EMAIL));
//            }
//        };
//    }
//
//    public enum InsertParentShipActionField implements ComparableField {
//        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
//    }
}
