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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.common.action.Action;
import grakn.benchmark.common.action.write.InsertRelocationAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknOperation;
import grakn.client.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static grakn.benchmark.grakn.action.Model.RELOCATION;
import static grakn.benchmark.grakn.action.Model.RELOCATION_DATE;
import static grakn.benchmark.grakn.action.Model.RELOCATION_NEW_LOCATION;
import static grakn.benchmark.grakn.action.Model.RELOCATION_PREVIOUS_LOCATION;
import static grakn.benchmark.grakn.action.Model.RELOCATION_RELOCATED_PERSON;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknInsertRelocationAction extends InsertRelocationAction<GraknOperation, ConceptMap> {
    public GraknInsertRelocationAction(GraknOperation dbOperation, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(dbOperation, city, today, relocateeEmail, relocationCityName);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert relocatePersonQuery = query(relocateeEmail, relocationCityName, city.name(), today);
        return Action.singleResult(dbOperation.execute(relocatePersonQuery));
    }

    public static GraqlInsert query(String relocateeEmail, String relocationCityName, String cityName, LocalDateTime today) {
        return match(
                var(PERSON).isa(PERSON).has(EMAIL, relocateeEmail),
                var("new-city").isa(CITY).has(LOCATION_NAME, relocationCityName),
                var("old-city").isa(CITY).has(LOCATION_NAME, cityName)
        ).insert(
                var(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, PERSON)
                        .isa(RELOCATION)
                        .has(RELOCATION_DATE, today)
        );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertRelocationActionField.PERSON_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
            put(InsertRelocationActionField.NEW_CITY_NAME, dbOperation.getOnlyAttributeOfThing(answer, "new-city", LOCATION_NAME));
            put(InsertRelocationActionField.RELOCATION_DATE, dbOperation.getOnlyAttributeOfThing(answer, RELOCATION, RELOCATION_DATE));
        }};
    }
}
