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

import com.vaticle.typedb.benchmark.common.concept.City;
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Parentship;
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

import static com.vaticle.typedb.benchmark.common.params.Context.LENGTH_OF_MARRIAGE_BEFORE_PARENTSHIP;
import static com.vaticle.typedb.common.collection.Collections.list;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

public abstract class ParentshipAgent<TX extends Transaction> extends Agent<City, TX> {

    protected ParentshipAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return ParentshipAgent.class;
    }

    @Override
    protected List<City> regions() {
        return context.seedData().cities();
    }

    @Override
    protected List<Report> run(Session<TX> session, City city, RandomSource random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.transaction()) {
            LocalDateTime marriageDate = context.today().minusYears(LENGTH_OF_MARRIAGE_BEFORE_PARENTSHIP);
             List<Marriage> marriages = matchMarriages(tx, city)
                    .sorted(comparing(Marriage::licence)).collect(toCollection(ArrayList::new));
             List<Person> newBorns = matchNewborns(tx, city, context.today())
                     .sorted(comparing(Person::email)).collect(toCollection(ArrayList::new));
            List<Pair<Marriage, Person>> parentships = random.randomAllocation(marriages, newBorns);

            parentships.forEach(parentship -> {
                Optional<Parentship> inserted = insertParentShip(tx, parentship.first().wife().email(),
                                                                 parentship.first().husband().email(),
                                                                 parentship.second().email());
                if (context.isReporting()) {
                    assert inserted.isPresent();
                    reports.add(new Report(list(parentship.first().wife().email(), parentship.first().husband().email(),
                                                parentship.second().email()),
                                           list(inserted.get().mother().email(), inserted.get().father().email(),
                                                inserted.get().child().email())));
                } else assert inserted.isEmpty();
            });
            tx.commit();
        }
        return reports;
    }

    protected abstract Stream<Person> matchNewborns(TX tx, City city, LocalDateTime today);

    protected abstract Stream<Marriage> matchMarriages(TX tx, City city);

    protected abstract Optional<Parentship> insertParentShip(TX tx, String motherEmail, String fatherEmail, String childEmail);
}
