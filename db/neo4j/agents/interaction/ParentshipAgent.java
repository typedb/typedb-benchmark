package grakn.simulation.db.neo4j.agents.interaction;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;
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

        Object[] parameters = new Object[]{
                "locationName", city().name()
        };

        Neo4jQuery query = new Neo4jQuery(template, parameters);

        log().query("getMarriageEmails", query);
        Result result = run(tx(), query);

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

        Object[] parameters = new Object[]{
                "dateOfBirth", today(),
                "locationName", city().name()
        };

        Neo4jQuery childrenQuery = new Neo4jQuery(template, parameters);

        log().query("getChildrenEmails", childrenQuery);
        return getOrderedAttribute(tx(), childrenQuery, "child.email");
    }

    @Override
    protected void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails) {

        String template = "" +
                "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)";

        for (String childEmail : childEmails) {
            Object[] parameters = new Object[]{
                    "motherEmail", marriage.get(Email.WIFE),
                    "fatherEmail", marriage.get(Email.HUSBAND),
                    "childEmail", childEmail
            };
            Neo4jQuery parentshipQuery = new Neo4jQuery(template, parameters);
            log().query("insertParentShip", parentshipQuery);
            run(tx(), parentshipQuery);
        }
    }
}
