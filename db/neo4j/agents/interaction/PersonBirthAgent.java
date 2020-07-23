package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class PersonBirthAgent extends grakn.simulation.db.common.agents.interaction.PersonBirthAgent<Neo4jDriverWrapper.Session, Neo4jDriverWrapper.Transaction> {

    @Override
    protected void insertPerson(String email, String gender, String forename, String surname) {
        String template = "MATCH (c:City {locationName: $locationName})" +
                "CREATE (p:Person {" +
                "email: $email, " +
                "dateOfBirth: $date, " +
                "gender: $gender, " +
                "forename: $forename, " +
                "surname: $surname" +
                "})-[:BORN_IN]->(c)," +
                "(p)-[:RESIDENT_OF {startDate: $date, isCurrent: $current}]->(c)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city().toString());
                put("email", email);
                put("date", today());
                put("gender", gender);
                put("forename", forename);
                put("surname", surname);
                put("current", true);
        }};

        Query query = new Query(template, parameters);

        log().query("insertPerson", query); //TODO Figure out to log Neo4j's pre-prepared queries
        tx().run(query);

//        Key constraints are possible with Neo4j Enterprise
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
//        CREATE CONSTRAINT ON (book:Book) ASSERT book.isbn IS UNIQUE
//        CREATE CONSTRAINT ON (book:Book) ASSERT exists(book.isbn)
    }
}
