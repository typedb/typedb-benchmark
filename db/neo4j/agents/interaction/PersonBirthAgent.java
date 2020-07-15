package grakn.simulation.db.neo4j.agents.interaction;

import org.neo4j.driver.Result;

import static org.neo4j.driver.Values.parameters;

public class PersonBirthAgent extends grakn.simulation.db.common.agents.interaction.PersonBirthAgent {

    @Override
    protected void insertPerson(String email, String gender, String forename, String surname) {

        Result result = tx().forNeo4j().run("MATCH (c:City {location_name: $location_name})" +
                "CREATE (p:Person {" +
                "email: $email, " +
                "date_of_birth: $date_of_birth, " +
                "gender: $gender, " +
                "forename: $forename, " +
                "surname: $surname" +
                "})-[:BORN_IN]->(c)",
                parameters(
                        "location_name", city().toString(),
                        "email", email,
                        "date_of_birth", today(),
                        "gender", gender,
                        "forename", forename,
                        "surname", surname
                        ));
//        log().query("insertPerson", query); //TODO Figure out to log Neo4j's pre-prepared queries

//        Key constraints are possible with Neo4j Enterprise
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
//        CREATE CONSTRAINT ON (book:Book) ASSERT book.isbn IS UNIQUE
//        CREATE CONSTRAINT ON (book:Book) ASSERT exists(book.isbn)
    }
}
