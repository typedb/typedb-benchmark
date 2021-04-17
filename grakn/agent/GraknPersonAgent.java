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

import grakn.benchmark.common.concept.City;
import grakn.benchmark.common.concept.Gender;
import grakn.benchmark.common.concept.Person;
import grakn.benchmark.common.params.Context;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.PersonAgent;
import grakn.client.api.answer.ConceptMap;
import grakn.common.collection.Pair;
import graql.lang.Graql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static grakn.benchmark.grakn.Labels.ADDRESS;
import static grakn.benchmark.grakn.Labels.BIRTH_DATE;
import static grakn.benchmark.grakn.Labels.BIRTH_PLACE;
import static grakn.benchmark.grakn.Labels.CHILD;
import static grakn.benchmark.grakn.Labels.CITY;
import static grakn.benchmark.grakn.Labels.CODE;
import static grakn.benchmark.grakn.Labels.EMAIL;
import static grakn.benchmark.grakn.Labels.FIRST_NAME;
import static grakn.benchmark.grakn.Labels.GENDER;
import static grakn.benchmark.grakn.Labels.LAST_NAME;
import static grakn.benchmark.grakn.Labels.PERSON;
import static grakn.benchmark.grakn.Labels.PLACE;
import static grakn.benchmark.grakn.Labels.RESIDENCE;
import static grakn.benchmark.grakn.Labels.RESIDENT;
import static grakn.benchmark.grakn.Labels.RESIDENTSHIP;
import static grakn.common.collection.Collections.pair;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;

public class GraknPersonAgent extends PersonAgent<GraknTransaction> {

    public GraknPersonAgent(GraknClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Optional<Pair<Person, City>> insertPerson(GraknTransaction tx, String email, String firstName, String lastName,
                                                        String address, Gender gender, LocalDateTime birthDate, City city) {
        tx.query().insert(Graql.match(
                var(CITY).isa(CITY).has(CODE, city.code())
        ).insert(
                var("p").isa(PERSON).has(EMAIL, email).has(FIRST_NAME, firstName).has(LAST_NAME, lastName)
                        .has(ADDRESS, address).has(GENDER, gender.value()).has(BIRTH_DATE, birthDate),
                var().rel(PLACE, var(CITY)).rel(CHILD, var("p")).isa(BIRTH_PLACE),
                var().rel(RESIDENCE, var(CITY)).rel(RESIDENT, var("p")).isa(RESIDENTSHIP)
        ));
        if (context.isTest()) return report(tx, email);
        else return Optional.empty();
    }

    private Optional<Pair<Person, City>> report(GraknTransaction tx, String email) {
        List<ConceptMap> answers = tx.query().match(Graql.match(
                var(PERSON).isa(PERSON).has(EMAIL, email).has(FIRST_NAME, var(FIRST_NAME)).has(LAST_NAME, var(LAST_NAME))
                        .has(ADDRESS, var(ADDRESS)).has(GENDER, var(GENDER)).has(BIRTH_DATE, var(BIRTH_DATE)),
                var(CITY).has(CODE, var(CODE)),
                rel(CHILD, var(PERSON)).rel(PLACE, var(CITY)).isa(BIRTH_PLACE),
                rel(RESIDENT, var(PERSON)).rel(RESIDENCE, var(CITY)).isa(RESIDENTSHIP)
        )).collect(toList());
        assert answers.size() == 1;
        ConceptMap inserted = answers.get(0);
        Person person = new Person(email, inserted.get(FIRST_NAME).asAttribute().asString().getValue(),
                                   inserted.get(LAST_NAME).asAttribute().asString().getValue(),
                                   inserted.get(ADDRESS).asAttribute().asString().getValue(),
                                   Gender.of(inserted.get(GENDER).asAttribute().asString().getValue()),
                                   inserted.get(BIRTH_DATE).asAttribute().asDateTime().getValue());
        City city = new City(inserted.get(CODE).asAttribute().asString().getValue());
        return Optional.of(pair(person, city));
    }
}
