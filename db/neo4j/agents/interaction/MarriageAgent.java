package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.interaction.MarriageAgentBase;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.Session.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;

public class MarriageAgent extends MarriageAgentBase {

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
                "WHERE datetime(person.dateOfBirth) <= datetime(\"" + dobOfAdults() + "\")\n" +
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
    protected HashMap<Field, Object> insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail) {
        String template = "" +
                "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $cityName})\n" +
                "CREATE (husband)-[marriage:MARRIED_TO {id: $marriageIdentifier, locationName: city.locationName}]->(wife)" +
                "RETURN marriage.id, husband.email, wife.email, city.locationName";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("marriageIdentifier", marriageIdentifier);
                put("wifeEmail", wifeEmail);
                put("husbandEmail", husbandEmail);
                put("cityName", city().name());
        }};

        Query query = new Query(template, parameters);

        log().query("insertMarriage", query);
        List<Record> answers = ((Transaction) tx()).execute(query);

        Map<String, Object> answer = getOnlyElement(answers).asMap();

        return new HashMap<Field, Object>() {{
            put(MarriageAgentField.MARRIAGE_IDENTIFIER, answer.get("marriage.id"));
            put(MarriageAgentField.WIFE_EMAIL, answer.get("wife.email"));  // TODO we get back the variables matched for in an insert?
            put(MarriageAgentField.HUSBAND_EMAIL, answer.get("husband.email"));
            put(MarriageAgentField.CITY_NAME, answer.get("city.locationName"));
        }};
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
