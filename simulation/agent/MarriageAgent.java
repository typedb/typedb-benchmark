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
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.common.params.Context.AGE_OF_ADULTHOOD;
import static com.vaticle.typedb.common.collection.Collections.list;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

public abstract class MarriageAgent<TX extends Transaction> extends Agent<Country, TX> {

    protected MarriageAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return MarriageAgent.class;
    }

    @Override
    protected List<Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, Country country, RandomSource random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.transaction()) {
            LocalDateTime partnerBirthDate = context.today().minusYears(AGE_OF_ADULTHOOD);
             List<Person> women = matchPartner(tx, country, partnerBirthDate, Gender.FEMALE)
                    .sorted(comparing(Person::email)).collect(toCollection(ArrayList::new));
            List<Person> men = matchPartner(tx, country, partnerBirthDate, Gender.MALE)
                    .sorted(comparing(Person::email)).collect(toCollection(ArrayList::new));
            random.randomPairs(women, men).forEach(partners -> {
                String licence = String.valueOf(Objects.hash(partners.first().email(), partners.second().email()));
                Optional<Marriage> inserted = insertMarriage(tx, partners.first().email(),
                                                             partners.second().email(),
                                                             licence);
                if (context.isReporting()) {
                    assert inserted.isPresent();
                    reports.add(new Report(list(partners.first().email(), partners.second().email(), licence),
                                           list(inserted.get().wife().email(), inserted.get().husband().email(),
                                                inserted.get().licence())));
                } else assert inserted.isEmpty();
            });
            tx.commit();
        }
        return reports;
    }

    protected abstract Stream<Person> matchPartner(TX tx, Country country, LocalDateTime birthDate, Gender gender);

    protected abstract Optional<Marriage> insertMarriage(TX tx, String wifeEmail, String husbandEmail,
                                                         String marriageLicence);
}
