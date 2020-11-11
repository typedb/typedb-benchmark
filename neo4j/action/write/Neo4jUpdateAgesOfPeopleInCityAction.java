package grakn.simulation.neo4j.action.write;

import grakn.simulation.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import grakn.simulation.neo4j.schema.Schema;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<Neo4jOperation> {
    public Neo4jUpdateAgesOfPeopleInCityAction(Neo4jOperation dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation, today, city);
    }

    @Override
    public Integer run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(Schema.LOCATION_NAME, city.name());
            put("dateToday", today);
        }};
        dbOperation.execute(new Query(query(), parameters));
        return null;
    }

    public static String query() {
        return "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n" +
                "RETURN person.age";
    }
}
