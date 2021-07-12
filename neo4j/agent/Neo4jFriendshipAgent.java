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

package com.vaticle.typedb.benchmark.neo4j.agent;

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jClient;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.common.collection.Pair;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.neo4j.Labels.BIRTH_DATE;
import static com.vaticle.typedb.benchmark.neo4j.Labels.CODE;
import static com.vaticle.typedb.benchmark.neo4j.Labels.EMAIL;
import static com.vaticle.typedb.common.collection.Collections.pair;

public class Neo4jFriendshipAgent extends FriendshipAgent<Neo4jTransaction> {

    private static final String X = "x", Y = "y";

    public Neo4jFriendshipAgent(Neo4jClient client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchTeenagers(Neo4jTransaction tx, Country country, LocalDateTime birthDate) {
        String query = "MATCH (person:Person {birthDate: $birthDate})" +
                "-[:RESIDES_IN]->(city:City)-[:CONTAINED_IN]->(country:Country {code: $code}) \n" +
                "RETURN person.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(CODE, country.code());
            put(BIRTH_DATE, birthDate);
        }};
        return tx.execute(new Query(query, parameters)).stream().map(
                record -> new Person((String) record.asMap().get("person.email"))
        );
    }

    @Override
    protected Optional<Pair<Person, Person>> insertFriends(Neo4jTransaction tx, String email1, String email2) {
        String query = "MATCH " +
                "(x:Person {email: $email1}), \n" +
                "(y:Person {email: $email2}) \n" +
                "CREATE (x)-[:FRIENDS_WITH]->(y)";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("email1", email1);
            put("email2", email2);
        }};
        tx.execute(new Query(query, parameters));
        if (context.isReporting()) return report(tx, email1, email2);
        else return Optional.empty();
    }

    private Optional<Pair<Person, Person>> report(Neo4jTransaction tx, String email1, String email2) {
        String query = "MATCH " +
                "(x:Person {email: $email1}), \n" +
                "(y:Person {email: $email2}), \n" +
                "(x)-[:FRIENDS_WITH]->(y) \n" +
                "RETURN x.email, y.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("email1", email1);
            put("email2", email2);
        }};
        List<Record> answers = tx.execute(new Query(query, parameters));
        assert answers.size() == 1;
        Map<String, Object> inserted = answers.get(0).asMap();
        Person person1 = new Person((String) inserted.get(X + "." + EMAIL));
        Person person2 = new Person((String) inserted.get(Y + "." + EMAIL));
        return Optional.of(pair(person1, person2));
    }
}
