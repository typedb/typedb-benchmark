package grakn.simulation.db.neo4j.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertPersonAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.FORENAME;
import static grakn.simulation.db.neo4j.schema.Schema.GENDER;
import static grakn.simulation.db.neo4j.schema.Schema.IS_CURRENT;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.SURNAME;

public class Neo4jInsertPersonAction extends InsertPersonAction<Neo4jOperation, Record> {
    public Neo4jInsertPersonAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(dbOperation, city, today, email, gender, forename, surname);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(LOCATION_NAME, worldCity.name());
            put(EMAIL, email);
            put(DATE_OF_BIRTH, today);
            put(GENDER, gender);
            put(FORENAME, forename);
            put(SURNAME, surname);
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
//        TODO Key constraints are possible with Neo4j Enterprise, and some constraints are supported in Community
//        https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
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
