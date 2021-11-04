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
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Parenthood;
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

import static com.vaticle.typedb.common.collection.Collections.list;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

public abstract class ParenthoodAgent<TX extends Transaction> extends Agent<Country, TX> {

    protected ParenthoodAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return ParenthoodAgent.class;
    }

    @Override
    protected List<Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, Country country, RandomSource random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.writeTransaction()) {
            LocalDateTime marriageDate = context.today().minusYears(context.yearsBeforeParenthood());
            List<Marriage> marriages = matchMarriages(tx, country, marriageDate)
                    .sorted(comparing(Marriage::licence)).collect(toCollection(ArrayList::new));
            List<Person> newBorns = matchNewborns(tx, country, context.today())
                    .sorted(comparing(Person::email)).collect(toCollection(ArrayList::new));
            List<Pair<Marriage, Person>> parenthoods = random.randomAllocation(marriages, newBorns);

            parenthoods.forEach(parenthood -> {
                Optional<Parenthood> inserted = insertParenthood(tx, parenthood.first().wife().email(),
                                                                 parenthood.first().husband().email(),
                                                                 parenthood.second().email());
                if (context.isReporting()) {
                    assert inserted.isPresent();
                    reports.add(new Report(list(parenthood.first().wife().email(), parenthood.first().husband().email(),
                                                parenthood.second().email()), list(inserted.get())));
                } else assert inserted.isEmpty();
            });
            tx.commit();
        }
        return reports;
    }

    protected abstract Stream<Person> matchNewborns(TX tx, Country country, LocalDateTime today);

    protected abstract Stream<Marriage> matchMarriages(TX tx, Country country, LocalDateTime marriageDate);

    protected abstract Optional<Parenthood> insertParenthood(TX tx, String motherEmail, String fatherEmail, String childEmail);
}
