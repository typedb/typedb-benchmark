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

package grakn.benchmark.grakn.action.write;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.benchmark.simulation.common.World;
import grakn.common.collection.Pair;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlMatch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static grakn.benchmark.grakn.action.Model.AGE;
import static grakn.benchmark.grakn.action.Model.BORN_IN;
import static grakn.benchmark.grakn.action.Model.BORN_IN_CHILD;
import static grakn.benchmark.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.CITY;
import static grakn.benchmark.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.benchmark.grakn.action.Model.EMAIL;
import static grakn.benchmark.grakn.action.Model.LOCATION_NAME;
import static grakn.benchmark.grakn.action.Model.PERSON;
import static grakn.common.collection.Collections.pair;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<GraknTransaction> {

    public GraknUpdateAgesOfPeopleInCityAction(GraknTransaction tx, LocalDateTime today, World.City city) {
        super(tx, today, city);
    }

    @Override
    public Void run() {
        getPeopleBornInCity(city).forEach(person -> {
            String email = person.first();
            LocalDateTime dob = person.second();
            long age = ChronoUnit.YEARS.between(dob, today);
            updatePersonAge(email, age);
        });
        return null;
    }

    private Stream<Pair<String, LocalDateTime>> getPeopleBornInCity(World.City worldCity) {
        GraqlMatch peopleQuery = getPeopleBornInCityQuery(worldCity.name());
        return tx.executeAsync(peopleQuery).map(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asThing().asAttribute().getValue();
            String email = personAnswer.get(EMAIL).asThing().asAttribute().getValue().toString();
            return pair(email, dob);
        });
    }

    public static GraqlMatch getPeopleBornInCityQuery(String worldCityName) {
        UnboundVariable city = var(CITY);
        UnboundVariable person = var(PERSON);
        UnboundVariable bornIn = var(BORN_IN);
        UnboundVariable dobVar = var(DATE_OF_BIRTH);
        UnboundVariable emailVar = var(EMAIL);

        return match(
                city.isa(CITY).has(LOCATION_NAME, worldCityName),
                person.isa(PERSON).has(EMAIL, emailVar).has(DATE_OF_BIRTH, dobVar),
                bornIn.rel(BORN_IN_CHILD, person).rel(BORN_IN_PLACE_OF_BIRTH, city).isa(BORN_IN)
        );
    }

    private void updatePersonAge(String personEmail, long newAge) {
        tx.executeAsync(
                match(var(PERSON).isa(PERSON).has(EMAIL, personEmail).has(AGE, var(AGE)))
                        .delete(var(PERSON).has(var(AGE)))
                        .insert(var(PERSON).has(AGE, newAge))
        );
    }
}
