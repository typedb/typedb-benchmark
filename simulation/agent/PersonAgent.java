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
import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;
import com.vaticle.typedb.common.collection.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.vaticle.typedb.benchmark.common.concept.Gender.FEMALE;
import static com.vaticle.typedb.benchmark.common.concept.Gender.MALE;
import static com.vaticle.typedb.common.collection.Collections.list;

public abstract class PersonAgent<TX extends Transaction> extends Agent<Country, TX> {

    private static final Logger LOG = LoggerFactory.getLogger(PersonAgent.class);

    protected PersonAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return PersonAgent.class;
    }

    @Override
    protected List<Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, Country country, RandomSource random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.transaction()) {
            for (int i = 0; i < context.scaleFactor(); i++) {
                Gender gender = random.nextBoolean() ? MALE : FEMALE;
                String firstName = random.choose(country.continent().commonFirstNames(gender));
                String lastName = random.choose(country.continent().commonLastNames());
                City city = random.choose(country.cities());
                String email = String.format("%s.%s.%s.%s@email.com", firstName, lastName, city.code(), random.nextInt());
                String address = random.address(city);
                Optional<Pair<Person, City>> inserted = insertPerson(tx, email, firstName, lastName, address, gender, context.today(), city);
                if (context.isReporting()) {
                    assert inserted.isPresent();
                    reports.add(new Report(list(email, firstName, lastName, address, gender, context.today(), city),
                                           list(inserted.get().first(), inserted.get().second())));
                } else assert inserted.isEmpty();
            }
            tx.commit();
        }
        return reports;
    }

    protected abstract Optional<Pair<Person, City>> insertPerson(TX tx, String email, String firstName, String lastName, String address,
                                                                 Gender gender, LocalDateTime birthDate, City city);
}
