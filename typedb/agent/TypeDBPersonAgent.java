/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.typedb.agent;

import com.vaticle.typedb.benchmark.common.concept.City;
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typeql.lang.TypeQL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.vaticle.typedb.benchmark.typedb.Labels.ADDRESS;
import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_PLACE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CHILD;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.EMAIL;
import static com.vaticle.typedb.benchmark.typedb.Labels.FIRST_NAME;
import static com.vaticle.typedb.benchmark.typedb.Labels.GENDER;
import static com.vaticle.typedb.benchmark.typedb.Labels.LAST_NAME;
import static com.vaticle.typedb.benchmark.typedb.Labels.PERSON;
import static com.vaticle.typedb.benchmark.typedb.Labels.PLACE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP;
import static com.vaticle.typedb.common.collection.Collections.pair;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;
import static java.util.stream.Collectors.toList;

public class TypeDBPersonAgent extends PersonAgent<TypeDBTransaction> {

    public TypeDBPersonAgent(TypeDBClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Optional<Pair<Person, City>> insertPerson(TypeDBTransaction tx, String email, String firstName, String lastName,
                                                        String address, Gender gender, LocalDateTime birthDate, City city) {
        tx.query().insert(TypeQL.match(
                var(CITY).isa(CITY).has(CODE, city.code())
        ).insert(
                var("p").isa(PERSON).has(EMAIL, email).has(FIRST_NAME, firstName).has(LAST_NAME, lastName)
                        .has(ADDRESS, address).has(GENDER, gender.value()).has(BIRTH_DATE, birthDate),
                var().rel(PLACE, var(CITY)).rel(CHILD, var("p")).isa(BIRTH_PLACE),
                var().rel(RESIDENCE, var(CITY)).rel(RESIDENT, var("p")).isa(RESIDENTSHIP)
        ));
        if (context.isReporting()) return report(tx, email);
        else return Optional.empty();
    }

    private Optional<Pair<Person, City>> report(TypeDBTransaction tx, String email) {
        List<ConceptMap> answers = tx.query().match(TypeQL.match(
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
