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

import grakn.benchmark.common.concept.Country;
import grakn.benchmark.common.concept.Person;
import grakn.benchmark.common.params.Context;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.client.api.answer.ConceptMap;
import grakn.common.collection.Pair;
import graql.lang.Graql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static grakn.benchmark.grakn.Labels.BIRTH_DATE;
import static grakn.benchmark.grakn.Labels.CITY;
import static grakn.benchmark.grakn.Labels.CODE;
import static grakn.benchmark.grakn.Labels.CONTAINED;
import static grakn.benchmark.grakn.Labels.CONTAINER;
import static grakn.benchmark.grakn.Labels.CONTAINS;
import static grakn.benchmark.grakn.Labels.COUNTRY;
import static grakn.benchmark.grakn.Labels.EMAIL;
import static grakn.benchmark.grakn.Labels.FRIEND;
import static grakn.benchmark.grakn.Labels.FRIENDSHIP;
import static grakn.benchmark.grakn.Labels.PERSON;
import static grakn.benchmark.grakn.Labels.RESIDENCE;
import static grakn.benchmark.grakn.Labels.RESIDENT;
import static grakn.benchmark.grakn.Labels.RESIDENTSHIP;
import static grakn.common.collection.Collections.pair;
import static graql.lang.Graql.eq;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;

public class GraknFriendshipAgent extends FriendshipAgent<GraknTransaction> {

    private static final String X = "x", Y = "y";
    private static final String E1 = "e1", E2 = "e2";

    public GraknFriendshipAgent(GraknClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchTeenagers(GraknTransaction tx, Country country, LocalDateTime birthDate) {
        return tx.query().match(Graql.match(
                var(PERSON).isa(PERSON).has(BIRTH_DATE, eq(birthDate)).has(EMAIL, var(EMAIL)),
                var(COUNTRY).isa(COUNTRY).has(CODE, country.code()),
                rel(RESIDENT, var(PERSON)).rel(RESIDENCE, var(CITY)).isa(RESIDENTSHIP),
                rel(CONTAINED, var(CITY)).rel(CONTAINER, var(COUNTRY)).isa(CONTAINS)
        ).get(var(EMAIL))).map(conceptMap -> new Person(conceptMap.get(EMAIL).asAttribute().asString().getValue()));
    }

    @Override
    protected Optional<Pair<Person, Person>> insertFriends(GraknTransaction tx, String email1, String email2) {
        tx.query().insert(Graql.match(
                var(X).isa(PERSON).has(EMAIL, email1),
                var(Y).isa(PERSON).has(EMAIL, email2)
        ).insert(rel(FRIEND, var(X)).rel(FRIEND, var(Y)).isa(FRIENDSHIP)));
        if (context.isReporting()) return report(tx, email1, email2);
        else return Optional.empty();
    }

    private Optional<Pair<Person, Person>> report(GraknTransaction tx, String email1, String email2) {
        List<ConceptMap> answers = tx.query().match(Graql.match(
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
