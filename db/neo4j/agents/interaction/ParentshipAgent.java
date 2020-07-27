package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ParentshipAgent extends grakn.simulation.db.common.agents.interaction.ParentshipAgent {

    @Override
    protected List<HashMap<Email, String>> getMarriageEmails() {

        String template = "" +
                "MATCH (city:City {locationName: $locationName}),\n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "WHERE NOT (wife)<-[:CHILD_OF]-(:Person)-[:CHILD_OF]->(husband)\n" +
                "RETURN husband.email, wife.email\n" +
                "ORDER BY marriage.id ASC\n";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city().name());
        }};

        Query query = new Query(template, parameters);

        log().query("getMarriageEmails", query);
        Result result = ((Neo4jDriverWrapper.Session.Transaction) tx()).execute(query);

        return result.stream().map(Record::asMap).map(r -> new HashMap<Email, String>() {{
            put(Email.WIFE, r.get("wife.email").toString());
            put(Email.HUSBAND, r.get("husband.email").toString());
        }}).collect(toList());
    }

    @Override
    protected List<String> getChildrenEmailsBorn(LocalDateTime dateToday) {
        String template = "" +
                "MATCH (city:City {locationName: $locationName}),\n" +
                "(child:Person {dateOfBirth: $dateOfBirth})-[:BORN_IN]->(city)\n" +
                "RETURN child.email";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("dateOfBirth", today());
                put("locationName", city().name());
        }};

        Query childrenQuery = new Query(template, parameters);

        log().query("getChildrenEmails", childrenQuery);
        return ((Neo4jDriverWrapper.Session.Transaction) tx()).getOrderedAttribute(childrenQuery, "child.email", null);
    }

    @Override
    protected void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails) {

        String template = "" +
                "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)";

        for (String childEmail : childEmails) {
            HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                    put("motherEmail", marriage.get(Email.WIFE));
                    put("fatherEmail", marriage.get(Email.HUSBAND));
                    put("childEmail", childEmail);
            }};
            Query parentshipQuery = new Query(template, parameters);
            log().query("insertParentShip", parentshipQuery);
            ((Neo4jDriverWrapper.Session.Transaction) tx()).execute(parentshipQuery);
        }
    }
}
