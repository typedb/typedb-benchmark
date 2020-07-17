package grakn.simulation.db.neo4j.agents.interaction;

import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class PersonBirthAgent extends grakn.simulation.db.common.agents.interaction.PersonBirthAgent {

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

        Object[] parameters = new Object[]{
                "locationName", city().toString(),
                "email", email,
                "date", today(),
                "gender", gender,
                "forename", forename,
                "surname", surname,
                "current", true
        };

        Neo4jQuery query = new Neo4jQuery(template, parameters);

        log().query("insertPerson", query); //TODO Figure out to log Neo4j's pre-prepared queries
        run(tx().forNeo4j(), query);

//        Key constraints are possible with Neo4j Enterprise
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
//        CREATE CONSTRAINT ON (book:Book) ASSERT book.isbn IS UNIQUE
//        CREATE CONSTRAINT ON (book:Book) ASSERT exists(book.isbn)
    }
}
