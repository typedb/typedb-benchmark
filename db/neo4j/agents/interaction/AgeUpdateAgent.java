package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent<Neo4jDriverWrapper.Session, Neo4jDriverWrapper.Transaction> {

    @Override
    protected void updateAgesOfAllPeople() {
        String template = "" +
                "MATCH (person:Person), (city:City {locationName: $cityName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("cityName", city().name());
                put("dateToday", today());
        }};

        Query query = new Query(template, parameters);

        log().query("updateAgesOfAllPeople", query);
        tx().run(query);
    }
}
