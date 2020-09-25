package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.ParentshipAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static java.util.stream.Collectors.toList;

public class ParentshipAgent extends Neo4jAgent<World.City> implements ParentshipAgentBase {

    @Override
    public List<HashMap<SpouseType, String>> getMarriageEmails(World.City city) {
        String template = "" +
                "MATCH (city:City {locationName: $locationName}),\n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "WHERE NOT (wife)<-[:CHILD_OF]-(:Person)-[:CHILD_OF]->(husband)\n" +
                "RETURN husband.email, wife.email\n" +
                "ORDER BY marriage.id ASC\n";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("locationName", city.name());
        }};
        List<Record> records = tx().execute(new Query(template, parameters));
        return records.stream().map(Record::asMap).map(r -> new HashMap<SpouseType, String>() {{
            put(SpouseType.WIFE, r.get("wife.email").toString());
            put(SpouseType.HUSBAND, r.get("husband.email").toString());
        }}).collect(toList());
    }

    @Override
    public List<String> getChildrenEmailsBorn(World.City city, LocalDateTime today) {
        String template = "" +
                "MATCH (city:City {locationName: $locationName}),\n" +
                "(child:Person {dateOfBirth: $dateOfBirth})-[:BORN_IN]->(city)\n" +
                "RETURN child.email";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("dateOfBirth", today);
                put("locationName", city.name());
        }};
        return tx().getOrderedAttribute(new Query(template, parameters), "child.email", null);
    }

    @Override
    public AgentResult insertParentShip(HashMap<SpouseType, String> marriage, String childEmail) {
        String template = "" +
                "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)\n" +
                "RETURN mother.email, father.email, child.email";
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("motherEmail", marriage.get(SpouseType.WIFE));
                put("fatherEmail", marriage.get(SpouseType.HUSBAND));
                put("childEmail", childEmail);
        }};
        return single_result(tx().execute(new Query(template, parameters)));
    }

    @Override
    public AgentResult resultsForTesting(Record answer) {
        return new AgentResult() {
            {
                put(ParentshipField.WIFE_EMAIL, answer.asMap().get("mother." + EMAIL));
                put(ParentshipField.HUSBAND_EMAIL, answer.asMap().get("father." + EMAIL));
                put(ParentshipField.CHILD_EMAIL, answer.asMap().get("child." + EMAIL));
            }
        };
    }
}
