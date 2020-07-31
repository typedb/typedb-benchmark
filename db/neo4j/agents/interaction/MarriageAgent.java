package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.Session.Transaction;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

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
                "AND NOT EXISTS (residency.endDate)\n" +
                "RETURN person.email";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city().name());
                put("gender", gender);
        }};

        Query query = new Query(template, parameters);

        log().query(scope, query);
        List<String> results = ((Transaction) tx()).getOrderedAttribute(query, "person.email", null);
        log().message(scope, results.toString());
        return results;
    }

    @Override
    protected void insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {
        String template = "" +
                "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $cityName})\n" +
                "CREATE (husband)-[:MARRIED_TO {id: $marriageIdentifier, locationName: city.locationName}]->(wife)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("marriageIdentifier", marriageIdentifier);
                put("wifeEmail", wifeEmail);
                put("husbandEmail", husbandEmail);
                put("cityName", city().name());
        }};

        Query query = new Query(template, parameters);

        log().query("insertMarriage", query);
        ((Transaction) tx()).execute(query);
    }

    @Override
    protected int checkCount() {
        String template = "" +
                "MATCH (city:City {locationName: $cityName}), \n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "RETURN count(marriage), count(marriage.id), count(wife.email), count(husband.email)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("cityName", city().name());
        }};

        Query countQuery = new Query(template, parameters);

        log().query("checkCount", countQuery);
        return ((Transaction) tx()).count(countQuery);
    }
}
