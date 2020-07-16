package grakn.simulation.db.neo4j.agents.interaction;

import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent {

    @Override
    protected void updateAgesOfAllPeople() {
        String template = "" +
                "MATCH (person:Person), (city:City {locationName: $cityName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n";

        Object[] parameters = new Object[]{
                "cityName", city().name(),
                "dateToday", today()
        };

        Neo4jQuery query = new Neo4jQuery(template, parameters);

        log().query("insertMarriage", query);
        run(tx().forNeo4j(), query);
    }
}
