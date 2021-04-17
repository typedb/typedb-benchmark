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
import graql.lang.Graql;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

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
import static graql.lang.Graql.var;

public class GraknPersonAgent extends PersonAgent<GraknTransaction> {

    public GraknPersonAgent(GraknClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Optional<Person> insertPerson(GraknTransaction tx, String email, String firstName, String lastName,
                                            String address, Gender gender, LocalDateTime birthDate, City city) {
        Stream<ConceptMap> inserted = tx.query().insert(Graql.match(
                var(CITY).isa(CITY).has(CODE, city.code())
        ).insert(
                var("p").isa(PERSON).has(EMAIL, email).has(FIRST_NAME, firstName).has(LAST_NAME, lastName)
                        .has(ADDRESS, address).has(GENDER, gender.value()).has(BIRTH_DATE, birthDate),
                var().rel(PLACE, var(CITY)).rel(CHILD, var("p")).isa(BIRTH_PLACE),
                var().rel(RESIDENCE, var(CITY)).rel(RESIDENT, var("p")).isa(RESIDENTSHIP)
        ));
        if (context.isTest()) return report(inserted);
        else return Optional.empty();
    }

    private Optional<Person> report(Stream<ConceptMap> inserted) {
        return Optional.empty(); // TODO
    }
}
