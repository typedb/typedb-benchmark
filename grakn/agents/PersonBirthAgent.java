package grakn.simulation.grakn.agents;

import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

public class PersonBirthAgent extends grakn.simulation.agents.interaction.PersonBirthAgent {

    @Override
    protected void insertPerson(String email, String gender, String forename, String surname) {

//        Result result = tx().run("MATCH (c:City), c.location-name = $location-name" +
//                "CREATE (p:Person {" +
//                "email = $email" +
//                "date-of-birth = $date-of-birth" +
//                "gender = $gender" +
//                "forename = $forename" +
//                "surname = $surname" +
//                "})-[:BORN_IN]->(c)",
//                parameters(
//                        "location-name", city().toString(),
//                        "email", email,
//                        "date-of-birth", today(),
//                        "gender", gender,
//                        "forename", forename,
//                        "surname", surname
//                        ));

//        Key constraints are possible with Neo4j Enterprise
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
//        CREATE CONSTRAINT ON (book:Book) ASSERT book.isbn IS UNIQUE
//        CREATE CONSTRAINT ON (book:Book) ASSERT exists(book.isbn)

        GraqlInsert query =
                Graql.match(
                        Graql.var("c").isa("city")
                                .has("location-name", city().toString()))
                        .insert(Graql.var("p").isa("person")
                                        .has("email", email)
                                        .has("date-of-birth", today())
                                        .has("gender", gender)
                                        .has("forename", forename)
                                        .has("surname", surname),
                                Graql.var("b").isa("born-in")
                                        .rel("born-in_child", "p")
                                        .rel("born-in_place-of-birth", "c")
                        );
        log().query("insertPerson", query);
        tx().forGrakn().execute(query);
    }
}
