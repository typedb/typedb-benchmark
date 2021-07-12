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
import com.vaticle.typedb.benchmark.common.concept.Marriage;
import com.vaticle.typedb.benchmark.common.concept.Parentship;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.neo4j.driver.Neo4jTransaction;
import com.vaticle.typedb.benchmark.simulation.agent.ParentshipAgent;
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
import static com.vaticle.typedb.benchmark.neo4j.Labels.MARRIAGE_DATE;

public class Neo4jParentshipAgent extends ParentshipAgent<Neo4jTransaction> {

    private static final String M = "m", F = "f", C = "c";

    public Neo4jParentshipAgent(Client<?, Neo4jTransaction> client, Context context) {
        super(client, context);
    }

    @Override
    protected Stream<Person> matchNewborns(Neo4jTransaction tx, Country country, LocalDateTime today) {
        String query = "MATCH (person:Person {birthDate: $birthDate})" +
                "-[:RESIDES_IN]->(:City)-[:CONTAINED_IN]->(country:Country {code: $code}) \n" +
                "RETURN person.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(CODE, country.code());
            put(BIRTH_DATE, today);
        }};
        return tx.execute(new Query(query, parameters)).stream().map(
                record -> new Person((String) record.asMap().get("person.email"))
        );
    }

    @Override
    protected Stream<Marriage> matchMarriages(Neo4jTransaction tx, Country country, LocalDateTime marriageDate) {
        String query = "MATCH (w:Person)-[:RESIDES_IN]->(:City)-[:CONTAINED_IN]->(country:Country {code: $code}),\n" +
                "(w)-[m:MARRIED_TO {marriageDate: $marriageDate}]->(h:Person)" +
                "RETURN w.email, h.email, m.marriageLicence";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(MARRIAGE_DATE, marriageDate);
            put(CODE, country.code());
        }};
        tx.execute(new Query(query, parameters));
        return tx.execute(new Query(query, parameters)).stream().map(
                record -> new Marriage(
                        new Person((String) record.asMap().get("w.email")),
                        new Person((String) record.asMap().get("h.email")),
                        (String) record.asMap().get("m.marriageLicence"),
                        (LocalDateTime) record.asMap().get("m.marriageDate")));
    }

    @Override
    protected Optional<Parentship> insertParentShip(Neo4jTransaction tx, String motherEmail, String fatherEmail,
                                                    String childEmail) {
        String query = "MATCH " +
                "(m:Person {email: $motherEmail}),\n" +
                "(f:Person {email: $fatherEmail}),\n" +
                "(c:Person {email: $childEmail})\n" +
                "CREATE (m)-[:PARENT_OF]->(c),\n" +
                "(f)-[:PARENT_OF]->(c)";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("motherEmail", motherEmail);
            put("fatherEmail", fatherEmail);
            put("childEmail", childEmail);
        }};
        tx.execute(new Query(query, parameters));
        if (context.isReporting()) return report(tx, motherEmail, fatherEmail, childEmail);
        else return Optional.empty();
    }

    private Optional<Parentship> report(Neo4jTransaction tx, String motherEmail, String fatherEmail, String childEmail) {
        String query = "MATCH " +
                "(m:Person {email: $motherEmail}),\n" +
                "(f:Person {email: $fatherEmail}),\n" +
                "(c:Person {email: $childEmail}),\n" +
                "(m)-[:PARENT_OF]->(c),\n" +
                "(f)-[:PARENT_OF]->(c)\n" +
                "RETURN m.email, f.email, c.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("motherEmail", motherEmail);
            put("fatherEmail", fatherEmail);
            put("childEmail", childEmail);
        }};
        List<Record> answers = tx.execute(new Query(query, parameters));
        assert answers.size() == 1;
        Map<String, Object> inserted = answers.get(0).asMap();
        Person mother = new Person((String) inserted.get(M + "." + EMAIL));
        Person father = new Person((String) inserted.get(F + "." + EMAIL));
        Person child = new Person((String) inserted.get(C + "." + EMAIL));
        return Optional.of(new Parentship(mother, father, child));
    }
}
