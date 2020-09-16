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

package grakn.simulation.grakn.action.write;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.grakn.action.Model.AGE;
import static grakn.simulation.grakn.action.Model.BORN_IN;
import static grakn.simulation.grakn.action.Model.BORN_IN_CHILD;
import static grakn.simulation.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<GraknOperation> {
    public GraknUpdateAgesOfPeopleInCityAction(GraknOperation dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation, today, city);
    }

    @Override
    public Integer run() {
        // Get all people born in a city
        HashMap<String, LocalDateTime> peopleAnswers;
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("getPeopleBornInCity")) {
            peopleAnswers = getPeopleBornInCity(city);
        }
        // Update their ages
        peopleAnswers.forEach((personEmail, personDob) -> {
                    long age = ChronoUnit.YEARS.between(personDob, today);
                    try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("updatePersonAge")) {
                        updatePersonAge(personEmail, age);
                    }
                }
        );
        return null;
    }

    public static GraqlMatch.Sorted getPeopleBornInCityQuery(String worldCityName) {
        UnboundVariable city = Graql.var(CITY);
        UnboundVariable person = Graql.var(PERSON);
        UnboundVariable bornIn = Graql.var(BORN_IN);
        UnboundVariable dobVar = Graql.var(DATE_OF_BIRTH);
        UnboundVariable emailVar = Graql.var(EMAIL);

        return Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, worldCityName),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar),
                bornIn
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
                        .isa(BORN_IN)
        ).sort(EMAIL);
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity(World.City worldCity) {
        GraqlMatch.Sorted peopleQuery = getPeopleBornInCityQuery(worldCity.name());

        HashMap<String, LocalDateTime> peopleDobs = new HashMap<>();
        dbOperation.execute(peopleQuery).forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asThing().asAttribute().getValue();
            String email = personAnswer.get(EMAIL).asThing().asAttribute().getValue().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }

    private void updatePersonAge(String personEmail, long newAge) {
        dbOperation.execute(deleteHasQuery(personEmail));
        dbOperation.execute(insertNewAgeQuery(personEmail, newAge));
    }

    public static GraqlInsert insertNewAgeQuery(String personEmail, long newAge) {
        UnboundVariable person = Graql.var(PERSON);
        return Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                person
                        .has(AGE, newAge)
        );
    }

    public static GraqlDelete deleteHasQuery(String personEmail) {
        UnboundVariable person = Graql.var(PERSON);
        UnboundVariable age = Graql.var(AGE);
        return Graql.match(
                    person
                            .isa(PERSON)
                            .has(EMAIL, personEmail)
                            .has(AGE, age)
            ).delete(
                    person
                            .has(AGE, age
                            )
            );
    }
}
