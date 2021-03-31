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
import grakn.benchmark.common.action.write.InsertPersonAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.grakn.driver.GraknOperation;
import grakn.client.api.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.grakn.action.Model.BORN_IN;
import static grakn.benchmark.grakn.action.Model.BORN_IN_CHILD;
import static grakn.benchmark.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.FORENAME;
import static grakn.benchmark.grakn.action.Model.GENDER;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static grakn.benchmark.grakn.action.Model.SURNAME;
import static graql.lang.Graql.var;

public class GraknInsertPersonAction extends InsertPersonAction<GraknOperation, ConceptMap> {
    public GraknInsertPersonAction(GraknOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public ConceptMap run() {
        GraqlInsert query = query(worldCity.name(), email, gender, forename, surname, today);
        return Action.singleResult(dbOperation.execute(query));
    }

    public static GraqlInsert query(String worldCityName, String email, String gender, String forename, String surname, LocalDateTime today) {
        UnboundVariable city = var(CITY);
        UnboundVariable person = var(PERSON);
        UnboundVariable bornIn = var(BORN_IN);
        return Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, worldCityName))
                .insert(
                        person.isa(PERSON)
                                .has(EMAIL, email)
                                .has(DATE_OF_BIRTH, today)
                                .has(GENDER, gender)
                                .has(FORENAME, forename)
                                .has(SURNAME, surname),
                        bornIn
                                .rel(BORN_IN_CHILD, person)
                                .rel(BORN_IN_PLACE_OF_BIRTH, city)
                                .isa(BORN_IN)
                );
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertPersonActionField.EMAIL, dbOperation.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
                put(InsertPersonActionField.DATE_OF_BIRTH, dbOperation.getOnlyAttributeOfThing(answer, PERSON, DATE_OF_BIRTH));
                put(InsertPersonActionField.GENDER, dbOperation.getOnlyAttributeOfThing(answer, PERSON, GENDER));
                put(InsertPersonActionField.FORENAME, dbOperation.getOnlyAttributeOfThing(answer, PERSON, FORENAME));
                put(InsertPersonActionField.SURNAME, dbOperation.getOnlyAttributeOfThing(answer, PERSON, SURNAME));
            }
        };
    }
}
