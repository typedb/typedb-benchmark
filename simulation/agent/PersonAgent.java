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

package grakn.benchmark.simulation.agent;

import grakn.benchmark.common.concept.City;
import grakn.benchmark.common.concept.Country;
import grakn.benchmark.common.concept.Gender;
import grakn.benchmark.common.concept.Person;
import grakn.benchmark.common.params.Context;
import grakn.benchmark.common.seed.RandomSource;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.common.collection.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static grakn.benchmark.common.concept.Gender.FEMALE;
import static grakn.benchmark.common.concept.Gender.MALE;
import static grakn.common.collection.Collections.list;

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
        try (TX tx = session.transaction(country.tracker(), context.iterationNumber())) {
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
                }
            }
            tx.commit();
        }
        return reports;
    }

    protected abstract Optional<Pair<Person, City>> insertPerson(TX tx, String email, String firstName, String lastName, String address,
                                                                 Gender gender, LocalDateTime birthDate, City city);
}
