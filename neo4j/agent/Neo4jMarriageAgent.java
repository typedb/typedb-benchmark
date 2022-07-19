/*
 * Copyright (C) 2022 Vaticle
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
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction;
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
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
import static com.vaticle.typedb.benchmark.neo4j.Labels.GENDER;
import static com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_DATE;
import static com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_LICENCE;


public class Neo4jMarriageAgent extends MarriageAgent<Neo4jTransaction> {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String M = "m";

    public Neo4jMarriageAgent(Client<?, Neo4jTransaction> client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchPartner(Neo4jTransaction tx, Country country, LocalDateTime birthDate,
                                          Gender gender) {
        String query = "MATCH (person:Person {birthDate: $birthDate, gender: $gender})" +
                "-[:RESIDES_IN]->(city:City)-[:CONTAINED_IN]->(country:Country {code: $code}) \n" +
                "RETURN person.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(CODE, country.code());
            put(BIRTH_DATE, birthDate);
            put(GENDER, gender.value());
        }};
        return tx.execute(new Query(query, parameters)).stream().map(
                record -> new Person((String) record.asMap().get("person.email"))
        );
    }

    @Override
    protected Optional<Marriage> insertMarriage(Neo4jTransaction tx, String wifeEmail, String husbandEmail,
                                                String marriageLicence, LocalDateTime marriageDate) {
        String query = "MATCH " +
                "(x:Person {email: $wifeEmail}), \n" +
                "(y:Person {email: $husbandEmail}) \n" +
                "CREATE (x)-[:MARRIED_TO {marriageLicence: $marriageLicence, marriageDate: $marriageDate}]->(y)";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("wifeEmail", wifeEmail);
            put("husbandEmail", husbandEmail);
            put("marriageLicence", marriageLicence);
            put("marriageDate", marriageDate);
        }};
        tx.execute(new Query(query, parameters));
        if (context.isReporting()) return report(tx, wifeEmail, husbandEmail, marriageLicence, marriageDate);
        else return Optional.empty();
    }

    private Optional<Marriage> report(Neo4jTransaction tx, String wifeEmail, String husbandEmail,
                                      String marriageLicence, LocalDateTime marriageDate) {
        String query = "MATCH " +
                "(x:Person {email: $wifeEmail}), \n" +
                "(y:Person {email: $husbandEmail}), \n" +
                "(x)-[m:MARRIED_TO {marriageLicence: $marriageLicence, marriageDate: $marriageDate}]->(y) \n" +
                "RETURN x.email, y.email, m.marriageLicence, m.marriageDate";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("wifeEmail", wifeEmail);
            put("husbandEmail", husbandEmail);
            put(MARRIAGE_LICENCE, marriageLicence);
            put(MARRIAGE_DATE, marriageDate);
        }};
        List<Record> answers = tx.execute(new Query(query, parameters));
        assert answers.size() == 1;
        Map<String, Object> inserted = answers.get(0).asMap();
        Person person1 = new Person((String) inserted.get(X + "." + EMAIL));
        Person person2 = new Person((String) inserted.get(Y + "." + EMAIL));
        return Optional.of(new Marriage(person1, person2, (String) inserted.get(M + "." + MARRIAGE_LICENCE),
                                        (LocalDateTime) inserted.get(M + "." + MARRIAGE_DATE)));
    }
}
