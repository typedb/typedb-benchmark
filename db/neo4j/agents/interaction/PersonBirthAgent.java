package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.PersonBirthAgentBase;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.Session.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;

public class PersonBirthAgent extends PersonBirthAgentBase {

    String EMAIL = "email";
    String DATE_OF_BIRTH = "dateOfBirth";
    String GENDER = "gender";
    String FORENAME = "forename";
    String SURNAME = "surname";
    String CURRENT = "current";

    @Override
    protected HashMap<Field, Object> insertPerson(String email, String gender, String forename, String surname) {
        String template = "MATCH (c:City {locationName: $locationName})" +
                "CREATE (person:Person {" +
                "email: $email, " +
                "dateOfBirth: $dateOfBirth, " +
                "gender: $gender, " +
                "forename: $forename, " +
                "surname: $surname" +
                "})-[:BORN_IN]->(c)," +
                "(person)-[:RESIDENT_OF {startDate: $dateOfBirth, isCurrent: $current}]->(c)" +
                "RETURN person.email, person.dateOfBirth, person.gender, person.forename, person.surname";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city().toString());
                put(EMAIL, email);
                put(DATE_OF_BIRTH, today());
                put(GENDER, gender);
                put(FORENAME, forename);
                put(SURNAME, surname);
                put(CURRENT, true);
        }};

        Query query = new Query(template, parameters);

        log().query("insertPerson", query); //TODO Figure out to log Neo4j's pre-prepared queries
        List<Record> answers = ((Transaction) tx()).execute(query);

        Map<String, Object> answer = getOnlyElement(answers).asMap();

        return new HashMap<Field, Object>() {{
            put(PersonBirthAgentField.EMAIL, answer.get("person." + EMAIL));
            put(PersonBirthAgentField.DATE_OF_BIRTH, answer.get("person." + DATE_OF_BIRTH));
            put(PersonBirthAgentField.GENDER, answer.get("person." + GENDER));
            put(PersonBirthAgentField.FORENAME, answer.get("person." + FORENAME));
            put(PersonBirthAgentField.SURNAME, answer.get("person." + SURNAME));
        }};

//        TODO Key constraints are possible with Neo4j Enterprise, and some constraints are supported in Community
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
    }

    @Override
    protected int checkCount() {
        String template = "" +
                "MATCH " +
                "(p:Person)-[:BORN_IN]->(c:City {locationName: $locationName})" +
                "RETURN count(p), count(p.email), count(p.dateOfBirth), count(p.gender), count(p.forename), count(p.surname)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("locationName", city().toString());
        }};

        Query countQuery = new Query(template, parameters);

        log().query("checkCount", countQuery);
        return ((Transaction) tx()).count(countQuery);
    }
}
