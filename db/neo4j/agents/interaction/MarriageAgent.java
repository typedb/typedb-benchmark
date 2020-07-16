package grakn.simulation.db.neo4j.agents.interaction;

import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.ExecutorUtils.getOrderedAttribute;

public class MarriageAgent extends grakn.simulation.db.common.agents.interaction.MarriageAgent {

    @Override
    protected List<String> getSingleWomen() {
        return getSinglePeopleOfGenderQuery("female");
    }

    @Override
    protected List<String> getSingleMen() {
        return getSinglePeopleOfGenderQuery("male");
    }

    private List<String> getSinglePeopleOfGenderQuery(String gender) {
        String queryString = "" +
                "MATCH (person:Person {gender: $gender})-[residency:RESIDENT_OF]->(city:City {location_name: $location_name})\n" +
                "WHERE datetime(\"" + dobOfAdults() + "\") < datetime(person.date_of_birth)\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "AND NOT EXISTS (residency.end_date)\n" +
                "RETURN person.email";

        Object[] parameters = new Object[]{
                "location_name", city().toString(),
                "gender", gender
        };

        Neo4jQuery query = new Neo4jQuery(queryString, parameters);

        log().query("insertMarriage", query);
        return getOrderedAttribute(tx().forNeo4j(), query, "email");
    }

    @Override
    protected void insertMarriage(String wifeEmail, String husbandEmail) {

    }
}
