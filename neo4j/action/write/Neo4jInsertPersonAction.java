/*
 * Copyright (C) 2020 Grakn Labs
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

package grakn.benchmark.neo4j.action.write;

import grakn.benchmark.common.action.Action;
import grakn.benchmark.common.action.write.InsertPersonAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.DATE_OF_BIRTH;
import static grakn.benchmark.neo4j.action.Model.EMAIL;
import static grakn.benchmark.neo4j.action.Model.FORENAME;
import static grakn.benchmark.neo4j.action.Model.GENDER;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;
import static grakn.benchmark.neo4j.action.Model.SURNAME;

public class Neo4jInsertPersonAction extends InsertPersonAction<Neo4jOperation, Record> {
    public Neo4jInsertPersonAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put(LOCATION_NAME, worldCity.name());
            put(EMAIL, email);
            put(DATE_OF_BIRTH, today);
            put(GENDER, gender);
            put(FORENAME, forename);
            put(SURNAME, surname);
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (c:City {locationName: $locationName})" +
                "CREATE (person:Person {" +
                "email: $email, " +
                "dateOfBirth: $dateOfBirth, " +
                "gender: $gender, " +
                "forename: $forename, " +
                "surname: $surname" +
                "})-[:BORN_IN]->(c)" +
                "RETURN person.email, person.dateOfBirth, person.gender, person.forename, person.surname";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertPersonActionField.EMAIL, answer.asMap().get("person." + EMAIL));
                put(InsertPersonActionField.DATE_OF_BIRTH, answer.asMap().get("person." + DATE_OF_BIRTH));
                put(InsertPersonActionField.GENDER, answer.asMap().get("person." + GENDER));
                put(InsertPersonActionField.FORENAME, answer.asMap().get("person." + FORENAME));
                put(InsertPersonActionField.SURNAME, answer.asMap().get("person." + SURNAME));
            }
        };
    }
}
