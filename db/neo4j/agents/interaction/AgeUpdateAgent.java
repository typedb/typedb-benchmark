package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.Session.Transaction;
import org.neo4j.driver.Query;

import java.util.HashMap;

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent {

    @Override
    protected void updateAgesOfAllPeople() {
        String template = "" +
                "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $cityName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("cityName", city().name());
                put("dateToday", today());
        }};

        Query query = new Query(template, parameters);

        log().query("updateAgesOfAllPeople", query);
        ((Transaction) tx()).execute(query);
    }

    @Override
    protected int checkCount() {
        String template = "" +
                "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $cityName})\n" +
                "RETURN count(person.age)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("cityName", city().name());
        }};

        Query countQuery = new Query(template, parameters);

        log().query("checkCount", countQuery);
        return ((Transaction) tx()).count(countQuery);
    }
}
