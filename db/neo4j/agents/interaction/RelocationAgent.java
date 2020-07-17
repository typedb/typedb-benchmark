package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class RelocationAgent extends grakn.simulation.db.common.agents.interaction.RelocationAgent {

    static Neo4jQuery cityResidentsQuery(World.City city, LocalDateTime earliestDate) {
        String template = "" +
                "MATCH (resident:Person)-[residency:RESIDENT_OF]->(city:City {locationName: $locationName})" +
                "WHERE datetime(residency.startDate) <= datetime($earliestDate)\n" +
                "RETURN resident.email";
        Object[] parameters = new Object[]{
                "locationName", city.name(),
                "earliestDate", earliestDate
        };
        return new Neo4jQuery(template, parameters);
    }

    @Override
    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        Neo4jQuery cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        int numRelocations = world().getScaleFactor();
        return getOrderedAttribute(tx().forNeo4j(), cityResidentsQuery, "resident.email", numRelocations);
    }

    @Override
    protected List<String> getRelocationCityNames() {
        String template = "" +
                "MATCH (city:City)-[:LOCATED_IN*2]->(continent:Continent {locationName: $continentName})\n" +
                "WHERE NOT city.locationName = $cityName\n" +
                "RETURN city.locationName";

        Object[] parameters = new Object[]{
                "continentName", city().country().continent().name(),
                "cityName", city().name()
        };

        Neo4jQuery relocationCitiesQuery = new Neo4jQuery(template, parameters);

        log().query("getRelocationCityNames", relocationCitiesQuery);
        return getOrderedAttribute(tx().forNeo4j(), relocationCitiesQuery, "city.locationName");
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

        Object[] parameters = new Object[]{
                "email", email,
                "newCityName", newCityName,
                "relocationDate", today()
        };
        Neo4jQuery relocatePersonQuery = new Neo4jQuery(template, parameters);
        log().query("insertRelocation", relocatePersonQuery);
        run(tx().forNeo4j(), relocatePersonQuery);
    }
}
