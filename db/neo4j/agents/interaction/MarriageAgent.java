package grakn.simulation.db.neo4j.agents.interaction;

import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class MarriageAgent extends grakn.simulation.db.common.agents.interaction.MarriageAgent {

    @Override
    protected List<String> getSingleWomen() {
        return getSinglePeopleOfGenderQuery("getSingleWomen", "female");
    }

    @Override
    protected List<String> getSingleMen() {
        return getSinglePeopleOfGenderQuery("getSingleMen", "male");
    }

    private List<String> getSinglePeopleOfGenderQuery(String scope, String gender) {
        String template = "" +
                "MATCH (person:Person {gender: $gender})-[residency:RESIDENT_OF]->(city:City {locationName: $locationName})\n" +
                "WHERE datetime(\"" + dobOfAdults() + "\") < datetime(person.dateOfBirth)\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "AND NOT EXISTS (residency.end_date)\n" +
                "RETURN person.email";

        Object[] parameters = new Object[]{
                "locationName", city().toString(),
                "gender", gender
        };

        Neo4jQuery query = new Neo4jQuery(template, parameters);

        log().query(scope, query);
        return getOrderedAttribute(tx().forNeo4j(), query, "person.email");
    }

    @Override
    protected void insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {
        String template = "" +
                "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $cityName})\n" +
                "CREATE (husband)-[:MARRIED_TO {id: $marriageIdentifier, locationName: city.locationName}]->(wife)";

        Object[] parameters = new Object[]{
                "marriageIdentifier", marriageIdentifier,
                "wifeEmail", wifeEmail,
                "husbandEmail", husbandEmail,
                "cityName", city().name(),
        };

        Neo4jQuery query = new Neo4jQuery(template, parameters);

        log().query("insertMarriage", query);
        run(tx().forNeo4j(), query);
    }
}
