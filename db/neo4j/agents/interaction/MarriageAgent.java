package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.MarriageAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.GENDER;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.neo4j.schema.Schema.MARRIAGE_ID;

public class MarriageAgent extends Neo4jAgent<World.City> implements MarriageAgentBase {

    @Override
    public List<String> getSingleWomen(World.City city, LocalDateTime dobOfAdults) {
        return getUnmarriedPeopleOfGender(city, "female", dobOfAdults);
    }

    @Override
    public List<String> getSingleMen(World.City city, LocalDateTime dobOfAdults) {
        return getUnmarriedPeopleOfGender(city, "male", dobOfAdults);
    }

    public List<String> getUnmarriedPeopleOfGender(World.City city, String gender, LocalDateTime dobOfAdults) {
        String template = "" +
                "MATCH (person:Person {gender: $gender})-[residency:RESIDENT_OF]->(city:City {locationName: $locationName})\n" +
                "WHERE datetime(person.dateOfBirth) <= datetime(\"" + dobOfAdults + "\")\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "AND NOT EXISTS (residency.endDate)\n" +
                "RETURN person.email";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put(LOCATION_NAME, city.name());
                put(GENDER, gender);
        }};

        Query query = new Query(template, parameters);
        return tx().getOrderedAttribute(query, "person." + EMAIL, null);
    }

    @Override
    public AgentResult insertMarriage(World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        String template = "" +
                "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $locationName})\n" +
                "CREATE (husband)-[marriage:MARRIED_TO {marriageId: $marriageId, locationName: city.locationName}]->(wife)" +
                "RETURN marriage.marriageId, husband.email, wife.email, city.locationName";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put(MARRIAGE_ID, marriageIdentifier);
                put("wifeEmail", wifeEmail);
                put("husbandEmail", husbandEmail);
                put(LOCATION_NAME, city.name());
        }};

        Query query = new Query(template, parameters);
        return single_result(tx().execute(query));
    }

    @Override
    public AgentResult resultsForTesting(Record answer) {
        return new AgentResult() {{
            put(MarriageAgentField.MARRIAGE_IDENTIFIER, answer.asMap().get("marriage." + MARRIAGE_ID));
            put(MarriageAgentField.WIFE_EMAIL, answer.asMap().get("wife." + EMAIL));  // TODO we get back the variables matched for in an insert?
            put(MarriageAgentField.HUSBAND_EMAIL, answer.asMap().get("husband." + EMAIL));
            put(MarriageAgentField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
        }};
    }

//    protected int checkCount() {
//        String template = "" +
//                "MATCH (city:City {locationName: $locationName}), \n" +
//                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
//                "RETURN count(marriage), count(marriage.marriageId), count(wife.email), count(husband.email)";
//
//        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
//            put(LOCATION_NAME, city.name());
//        }};
//
//        Query countQuery = new Query(template, parameters);
//
//        log().query("checkCount", countQuery);
//        return tx().count(countQuery);
//    }
}
