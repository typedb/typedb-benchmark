package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.RelocationAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class RelocationAgent extends Neo4jAgent<World.City> implements RelocationAgentBase {

    static Query cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        String template = "" +
                "MATCH (resident:Person)-[residency:RESIDENT_OF]->(city:City {locationName: $locationName})" +
                "WHERE datetime(residency.startDate) <= datetime($earliestDate)\n" +
                "RETURN resident.email";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city.name());
                put("earliestDate", earliestDate);
        }};
        return new Query(template, parameters);
    }

    @Override
    public List<String> getResidentEmails(World.City city, LocalDateTime earliestDate, int numRelocations) {
        Query cityResidentsQuery = cityResidentsQuery(city, earliestDate);
        return tx().getOrderedAttribute(cityResidentsQuery, "resident.email", numRelocations);
    }

    @Override
    public List<String> getRelocationCityNames(World.City city) {
        String template = "" +
                "MATCH (city:City)-[:LOCATED_IN*2]->(continent:Continent {locationName: $continentName})\n" +
                "WHERE NOT city.locationName = $cityName\n" +
                "RETURN city.locationName";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("continentName", city.country().continent().name());
                put("cityName", city.name());
        }};
        return tx().getOrderedAttribute(new Query(template, parameters), "city.locationName", null);
    }

    @Override
    public void insertRelocation(World.City city, LocalDateTime today, String email, String newCityName) {
        // This raises questions over whether the person's residency end-date should be updated in this step, or
        // figured out at query-time, which would be more in-line with Grakn

        // In either case, their old residency should be given an `endDate`, and they should have a new residency
        // alongside this relation

        // As it is, not making this ternary is losing the information of where the person if relocating from
        String template = "" +
                "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("email", email);
                put("newCityName", newCityName);
                put("relocationDate", today);
        }};
        tx().execute(new Query(template, parameters));
    }
}
