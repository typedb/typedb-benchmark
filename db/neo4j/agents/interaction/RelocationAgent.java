package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.RelocationAgentBase;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.RELOCATION_DATE;

public class RelocationAgent extends Neo4jAgent<World.City> implements RelocationAgentBase {

    static Query cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        String template = "" +
                "MATCH (resident:Person)-[residentOf:RESIDENT_OF]->(city:City {locationName: $locationName})" +
                "WHERE datetime(residentOf.startDate) <= datetime($earliestDate) AND NOT EXISTS (residentOf.endDate)\n" +
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
    public AgentResult insertRelocation(World.City city, LocalDateTime today, String email, String newCityName) {
        // This raises questions over whether the person's ResidentOf end-date should be updated in this step, or
        // figured out at query-time, which would be more in-line with Grakn

        // In either case, their old ResidentOf should be given an `endDate`, and they should have a new ResidentOf
        // alongside this relation

        endPastResidencies(tx(), email, today);

        // As it is, not making this ternary is losing the information of where the person if relocating from
        String template = "" +
                "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[relocatedTo:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity), " +
                "(person)-[:RESIDENT_OF {startDate: $relocationDate, isCurrent: TRUE}]->(newCity)" +
                "RETURN person.email, newCity.locationName, relocatedTo.relocationDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("email", email);
                put("newCityName", newCityName);
                put("relocationDate", today);
        }};
        return single_result(tx().execute(new Query(template, parameters)));
    }

    public static void endPastResidencies(Transaction tx, String email, LocalDateTime today){
        String template = "" +
                "MATCH (person:Person {email: $email})-[residentOf:RESIDENT_OF]->(oldCity:City)\n" +
                "WHERE NOT EXISTS (residentOf.endDate)\n" +
                "SET residentOf.isCurrent = FALSE, residentOf.endDate = $endDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("email", email);
            put("endDate", today);
        }};
        tx.execute(new Query(template, parameters));
    }

    @Override
    public AgentResult resultsForTesting(Record answer) {
        return new AgentResult() {
            {
                put(RelocationAgentField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
                put(RelocationAgentField.NEW_CITY_NAME, answer.asMap().get("newCity." + LOCATION_NAME));
                put(RelocationAgentField.RELOCATION_DATE, answer.asMap().get("relocatedTo." + RELOCATION_DATE));
            }
        };
    }
}
