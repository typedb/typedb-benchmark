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
import grakn.benchmark.simulation.agent.RelocationAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.RELOCATION;
import static grakn.benchmark.grakn.agent.Types.RELOCATION_DATE;
import static grakn.benchmark.grakn.agent.Types.RELOCATION_NEW_LOCATION;
import static grakn.benchmark.grakn.agent.Types.RELOCATION_PREVIOUS_LOCATION;
import static grakn.benchmark.grakn.agent.Types.RELOCATION_RELOCATED_PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknRelocationAgent extends RelocationAgent<GraknTransaction> {

    public GraknRelocationAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(GraknTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return GraknMatcher.matchResidentsInCity(tx, city, numResidents, earliestDate);
    }

    @Override
    protected List<String> matchCitiesInContinent(GraknTransaction tx, GeoData.City city) {
        return GraknMatcher.matchCitiesInContinent(tx, city);
    }

    @Override
    protected void insertRelocation(GraknTransaction tx, GeoData.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        tx.execute(match(
                var(PERSON).isa(PERSON).has(EMAIL, residentEmail),
                var("new-city").isa(CITY).has(LOCATION_NAME, relocationCityName),
                var("old-city").isa(CITY).has(LOCATION_NAME, city.name())
        ).insert(
                var(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, PERSON)
                        .isa(RELOCATION)
                        .has(RELOCATION_DATE, today)
        ));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertRelocationActionField.PERSON_EMAIL, tx.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
//            put(InsertRelocationActionField.NEW_CITY_NAME, tx.getOnlyAttributeOfThing(answer, "new-city", LOCATION_NAME));
//            put(InsertRelocationActionField.RELOCATION_DATE, tx.getOnlyAttributeOfThing(answer, RELOCATION, RELOCATION_DATE));
//        }};
//    }
}
