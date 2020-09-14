package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.RelocationAgentBase;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.common.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class RelocationAgent extends RelocationAgentBase<Neo4jContext> {

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
    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        Query cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        int numRelocations = world().getScaleFactor();
        return ((Neo4jDriverWrapper.Session.Transaction) tx()).getOrderedAttribute(cityResidentsQuery, "resident.email", numRelocations);
    }

    @Override
    protected List<String> getRelocationCityNames() {
        String template = "" +
                "MATCH (city:City)-[:LOCATED_IN*2]->(continent:Continent {locationName: $continentName})\n" +
                "WHERE NOT city.locationName = $cityName\n" +
                "RETURN city.locationName";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("continentName", city().country().continent().name());
                put("cityName", city().name());
        }};

        Query relocationCitiesQuery = new Query(template, parameters);

        log().query("getRelocationCityNames", relocationCitiesQuery);
        return ((Neo4jDriverWrapper.Session.Transaction) tx()).getOrderedAttribute(relocationCitiesQuery, "city.locationName", null);
    }

    @Override
    protected void insertRelocation(String email, String newCityName) {
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
                put("relocationDate", today());
        }};
        Query relocatePersonQuery = new Query(template, parameters);
        log().query("insertRelocation", relocatePersonQuery);
        ((Neo4jDriverWrapper.Session.Transaction) tx()).execute(relocatePersonQuery);
    }

    @Override
    protected int checkCount() {
        return 0;
    }
}
