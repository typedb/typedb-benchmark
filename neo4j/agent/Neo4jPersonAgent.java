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

package grakn.benchmark.neo4j.agent;

import grakn.benchmark.common.concept.City;
import grakn.benchmark.common.concept.Gender;
import grakn.benchmark.common.concept.Person;
import grakn.benchmark.common.params.Context;
import grakn.benchmark.neo4j.driver.Neo4jClient;
import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.agent.PersonAgent;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static grakn.benchmark.neo4j.Labels.ADDRESS;
import static grakn.benchmark.neo4j.Labels.BIRTH_DATE;
import static grakn.benchmark.neo4j.Labels.CODE;
import static grakn.benchmark.neo4j.Labels.EMAIL;
import static grakn.benchmark.neo4j.Labels.FIRST_NAME;
import static grakn.benchmark.neo4j.Labels.GENDER;
import static grakn.benchmark.neo4j.Labels.LAST_NAME;

public class Neo4jPersonAgent extends PersonAgent<Neo4jTransaction> {

    public Neo4jPersonAgent(Neo4jClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Optional<Person> insertPerson(Neo4jTransaction tx, String email, String firstName, String lastName,
                                            String address, Gender gender, LocalDateTime birthDate, City city) {
        String query = "MATCH (c:City {code: $code}) " +
                "CREATE (person:Person {" +
                "email: $email, " +
                "firstName: $firstName, " +
                "lastName: $lastName" +
                "address: $address" +
                "gender: $gender, " +
                "birthDate: $birthDate, " +
                "})-[:BORN_IN]->(c), " +
                "(person)-[:RESIDES_IN]->(c)";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(CODE, city.code());
            put(EMAIL, email);
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastName);
            put(ADDRESS, address);
            put(GENDER, gender.value());
            put(BIRTH_DATE, birthDate);
        }};
        List<Record> inserted = tx.execute(new Query(query, parameters));
        if (context.isTest()) return report(inserted);
        else return Optional.empty();
    }

    private Optional<Person> report(List<Record> inserted) {
        return Optional.empty(); // TODO
    }
}
