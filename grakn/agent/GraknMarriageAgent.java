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
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
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
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknMarriageAgent extends MarriageAgent<GraknTransaction> {

    public GraknMarriageAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchUnmarriedPeopleInCity(GraknTransaction tx, GeoData.City city, String gender, LocalDateTime dobOfAdults) {
        return GraknMatcher.matchUnmarriedPeople(tx, city, gender, dobOfAdults);
    }

    @Override
    protected void insertMarriage(GraknTransaction tx, GeoData.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        UnboundVariable marriage = var("marriage");
        UnboundVariable husband = var("husband");
        UnboundVariable wife = var("wife");

        GraqlInsert marriageQuery = match(
                husband.isa(PERSON).has(EMAIL, husbandEmail),
                wife.isa(PERSON).has(EMAIL, wifeEmail),
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name())
        ).insert(
                marriage
                        .rel(MARRIAGE_HUSBAND, husband)
                        .rel(MARRIAGE_WIFE, wife)
                        .isa(MARRIAGE)
                        .has(MARRIAGE_ID, marriageIdentifier),
                var().rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, var(CITY)).isa(LOCATES)
        );
        tx.execute(marriageQuery);
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, tx.getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID));
//            put(InsertMarriageActionField.WIFE_EMAIL, tx.getOnlyAttributeOfThing(answer, "wife", EMAIL));
//            put(InsertMarriageActionField.HUSBAND_EMAIL, tx.getOnlyAttributeOfThing(answer, "husband", EMAIL));
//            put(InsertMarriageActionField.CITY_NAME, tx.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
//        }};
//    }
}
