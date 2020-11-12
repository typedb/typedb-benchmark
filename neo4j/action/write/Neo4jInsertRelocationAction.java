package grakn.simulation.neo4j.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.write.InsertRelocationAction;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.neo4j.action.Model.EMAIL;
import static grakn.simulation.neo4j.action.Model.LOCATION_NAME;
import static grakn.simulation.neo4j.action.Model.RELOCATION_DATE;

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

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("email", relocateeEmail);
            put("newCityName", relocationCityName);
            put("relocationDate", today);
        }};
        return Action.singleResult(dbOperation.execute(new Query(createRelocationQuery(), parameters)));
    }

    public static String createRelocationQuery() {
        // Not making this ternary is losing the information of where the person if relocating from
        return "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[relocatedTo:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity)" +
                "RETURN person.email, newCity.locationName, relocatedTo.relocationDate";
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
