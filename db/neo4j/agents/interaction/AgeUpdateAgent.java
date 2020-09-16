package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.AgeUpdateAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;

public class AgeUpdateAgent extends Neo4jAgent<World.City> implements AgeUpdateAgentBase {

    @Override
    public void updateAgesOfAllPeople(LocalDateTime today, World.City city) {
        String template = "" +
                "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n" +
                "RETURN person.age";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put(LOCATION_NAME, city.name());
                put("dateToday", today);
        }};

        Query query = new Query(template, parameters);

        log().query("updateAgesOfAllPeople", query);
        tx().execute(query);
    }

//    protected int checkCount() {
//        String template = "" +
//                "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $cityName})\n" +
//                "RETURN count(person.age)";
//
//        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
//            put("cityName", city.name());
//        }};
//
//        Query countQuery = new Query(template, parameters);
//
//        log().query("checkCount", countQuery);
//        return tx().count(countQuery);
//    }
}
