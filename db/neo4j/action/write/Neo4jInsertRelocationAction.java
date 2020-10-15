package grakn.simulation.db.neo4j.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.write.InsertRelocationAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.RELOCATION_DATE;

public class Neo4jInsertRelocationAction extends InsertRelocationAction<Neo4jOperation, Record> {
    public Neo4jInsertRelocationAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(dbOperation, city, today, relocateeEmail, relocationCityName);
    }

    @Override
    public Record run() {
        // This raises questions over whether the person's ResidentOf end-date should be updated in this step, or
        // figured out at query-time, which would be more in-line with Grakn

        // In either case, their old ResidentOf should be given an `endDate`, and they should have a new ResidentOf
        // alongside this relation

        endPastResidencies(dbOperation, relocateeEmail, today);

        // As it is, not making this ternary is losing the information of where the person if relocating from
        String template = "" +
                "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[relocatedTo:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity), " +
                "(person)-[:RESIDENT_OF {startDate: $relocationDate, isCurrent: TRUE}]->(newCity)" +
                "RETURN person.email, newCity.locationName, relocatedTo.relocationDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("email", relocateeEmail);
            put("newCityName", relocationCityName);
            put("relocationDate", today);
        }};
        return Action.singleResult(dbOperation.execute(new Query(template, parameters)));
    }

    public static void endPastResidencies(Neo4jOperation dbOperation, String email, LocalDateTime today){
        String template = "" +
                "MATCH (person:Person {email: $email})-[residentOf:RESIDENT_OF]->(oldCity:City)\n" +
                "WHERE NOT EXISTS (residentOf.endDate)\n" +
                "SET residentOf.isCurrent = FALSE, residentOf.endDate = $endDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("email", email);
            put("endDate", today);
        }};
        dbOperation.execute(new Query(template, parameters));
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertRelocationActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
            put(InsertRelocationActionField.NEW_CITY_NAME, answer.asMap().get("newCity." + LOCATION_NAME));
            put(InsertRelocationActionField.RELOCATION_DATE, answer.asMap().get("relocatedTo." + RELOCATION_DATE));
        }};
    }
}
