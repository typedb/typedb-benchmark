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

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBClient;
import com.vaticle.typedb.benchmark.typedb.driver.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typeql.lang.TypeQL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.typedb.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CITY;
import static com.vaticle.typedb.benchmark.typedb.Labels.CODE;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINED;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINER;
import static com.vaticle.typedb.benchmark.typedb.Labels.CONTAINS;
import static com.vaticle.typedb.benchmark.typedb.Labels.COUNTRY;
import static com.vaticle.typedb.benchmark.typedb.Labels.EMAIL;
import static com.vaticle.typedb.benchmark.typedb.Labels.FRIEND;
import static com.vaticle.typedb.benchmark.typedb.Labels.FRIENDSHIP;
import static com.vaticle.typedb.benchmark.typedb.Labels.PERSON;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENCE;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENT;
import static com.vaticle.typedb.benchmark.typedb.Labels.RESIDENTSHIP;
import static com.vaticle.typedb.common.collection.Collections.pair;
import static com.vaticle.typeql.lang.TypeQL.eq;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.var;
import static java.util.stream.Collectors.toList;

public class TypeDBFriendshipAgent extends FriendshipAgent<TypeDBTransaction> {

    private static final String X = "x", Y = "y";
    private static final String E1 = "e1", E2 = "e2";

    public TypeDBFriendshipAgent(TypeDBClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchTeenagers(TypeDBTransaction tx, Country country, LocalDateTime birthDate) {
        return tx.query().match(TypeQL.match(
                var(PERSON).isa(PERSON).has(BIRTH_DATE, eq(birthDate)).has(EMAIL, var(EMAIL)),
                var(COUNTRY).isa(COUNTRY).has(CODE, country.code()),
                rel(RESIDENT, var(PERSON)).rel(RESIDENCE, var(CITY)).isa(RESIDENTSHIP),
                rel(CONTAINED, var(CITY)).rel(CONTAINER, var(COUNTRY)).isa(CONTAINS)
        ).get(var(EMAIL))).map(conceptMap -> new Person(conceptMap.get(EMAIL).asAttribute().asString().getValue()));
    }

    @Override
    protected Optional<Pair<Person, Person>> insertFriends(TypeDBTransaction tx, String email1, String email2) {
        tx.query().insert(TypeQL.match(
                var(X).isa(PERSON).has(EMAIL, email1),
                var(Y).isa(PERSON).has(EMAIL, email2)
        ).insert(rel(FRIEND, var(X)).rel(FRIEND, var(Y)).isa(FRIENDSHIP)));
        if (context.isReporting()) return report(tx, email1, email2);
        else return Optional.empty();
    }

    private Optional<Pair<Person, Person>> report(TypeDBTransaction tx, String email1, String email2) {
        List<ConceptMap> answers = tx.query().match(TypeQL.match(
                var(X).isa(PERSON).has(EMAIL, var(E1)), var(E1).eq(email1),
                var(Y).isa(PERSON).has(EMAIL, var(E2)), var(E2).eq(email2),
                rel(FRIEND, var(X)).rel(FRIEND, var(Y)).isa(FRIENDSHIP)
        ).get(var(E1), var(E2))).collect(toList());
        assert answers.size() == 1;
        ConceptMap inserted = answers.get(0);
        Person person1 = new Person(inserted.get(E1).asAttribute().asString().getValue());
        Person person2 = new Person(inserted.get(E2).asAttribute().asString().getValue());
        return Optional.of(pair(person1, person2));
    }
}
