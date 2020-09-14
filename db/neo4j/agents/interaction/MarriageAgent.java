package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.MarriageAgentBase;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.common.Neo4jContext;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.Session.Transaction;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.GENDER;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.MARRIAGE_ID;

public class MarriageAgent extends MarriageAgentBase<Neo4jContext> {

    @Override
    protected List<String> getSingleWomen(LocalDateTime dobOfAdults) {
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
                put(LOCATION_NAME, city().name());
                put(GENDER, gender);
        }};

        Query query = new Query(template, parameters);

        log().query(scope, query);
        List<String> results = ((Transaction) tx()).getOrderedAttribute(query, "person." + EMAIL, null);
        log().message(scope, results.toString());
        return results;
    }

    @Override
    protected AgentResult insertMarriage(String scope, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        String template = "" +
                "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $locationName})\n" +
                "CREATE (husband)-[marriage:MARRIED_TO {marriageId: $marriageId, locationName: city.locationName}]->(wife)" +
                "RETURN marriage.marriageId, husband.email, wife.email, city.locationName";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put(MARRIAGE_ID, marriageIdentifier);
                put("wifeEmail", wifeEmail);
                put("husbandEmail", husbandEmail);
                put(LOCATION_NAME, city().name());
        }};

        Query query = new Query(template, parameters);

        log().query("insertMarriage", query);
        List<Record> answers = ((Transaction) tx()).execute(query);

        Map<String, Object> answer = getOnlyElement(answers).asMap();

        return new AgentResult() {{
            put(MarriageAgentField.MARRIAGE_IDENTIFIER, answer.get("marriage." + MARRIAGE_ID));
            put(MarriageAgentField.WIFE_EMAIL, answer.get("wife." + EMAIL));  // TODO we get back the variables matched for in an insert?
            put(MarriageAgentField.HUSBAND_EMAIL, answer.get("husband." + EMAIL));
            put(MarriageAgentField.CITY_NAME, answer.get("city." + LOCATION_NAME));
        }};
    }

    @Override
    protected int checkCount() {
        String template = "" +
                "MATCH (city:City {locationName: $locationName}), \n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "RETURN count(marriage), count(marriage.marriageId), count(wife.email), count(husband.email)";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(LOCATION_NAME, city().name());
        }};

        Query countQuery = new Query(template, parameters);

        log().query("checkCount", countQuery);
        return ((Transaction) tx()).count(countQuery);
    }
}
