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

package com.vaticle.typedb.benchmark.simulation.agent;

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;
import com.vaticle.typedb.common.collection.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.common.params.Context.AGE_OF_FRIENDSHIP;
import static com.vaticle.typedb.common.collection.Collections.list;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

public abstract class FriendshipAgent<TX extends Transaction> extends Agent<Country, TX> {

    protected FriendshipAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return FriendshipAgent.class;
    }

    @Override
    protected List<Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, Country country, RandomSource random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.transaction()) {
            LocalDateTime birthDate = context.today().minusYears(AGE_OF_FRIENDSHIP);
            ArrayList<Person> teenagers = matchTeenagers(tx, country, birthDate)
                    .sorted(comparing(Person::email)).collect(toCollection(ArrayList::new));
            random.randomPairs(teenagers, min(log2(context.scaleFactor()), 1)).forEach(friends -> {
                Optional<Pair<Person, Person>> inserted = insertFriends(tx, friends.first().email(), friends.second().email());
                if (context.isReporting()) {
                    assert inserted.isPresent();
                    reports.add(new Report(list(friends.first().email(), friends.second().email()),
                                           list(inserted.get().first(), inserted.get().second())));
                } else assert inserted.isEmpty();
            });
            tx.commit();
        }
        return reports;
    }

    public static int log2(int x) {
        return (int) (log(x) / log(2));
    }

    protected abstract Stream<Person> matchTeenagers(TX tx, Country country, LocalDateTime birthDate);

    protected abstract Optional<Pair<Person, Person>> insertFriends(TX tx, String email1, String email2);
}
