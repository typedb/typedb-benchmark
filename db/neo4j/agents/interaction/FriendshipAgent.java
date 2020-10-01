package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.interaction.FriendshipAgentBase;
import grakn.simulation.db.common.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.START_DATE;

public class FriendshipAgent extends Neo4jAgent<World.City> implements FriendshipAgentBase {

    @Override
    public List<String> getResidentEmails(World.City city, LocalDateTime earliestDate) {
        Query cityResidentsQuery = cityResidentsQuery(city, earliestDate);
        return tx().getOrderedAttribute(cityResidentsQuery, "resident.email", null);
    }

    @Override
    public ActionResult insertFriendship(LocalDateTime today, String friend1Email, String friend2Email) {
        String template = "" +
                "MATCH " +
                "(p1:Person),\n" +
                "(p2:Person)\n" +
                "WHERE p1.email = $p1Email AND p2.email = $p2Email AND NOT (p1)-[:FRIEND_OF]-(p2)\n" +
                "CREATE (p1)-[friendOf:FRIEND_OF {startDate: $startDate}]->(p2)\n" +
                "RETURN p1.email, p2.email, friendOf.startDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
                put("p1Email", friend1Email);
                put("p2Email", friend2Email);
                put("startDate", today);
        }};
        Query insertFriendshipQuery = new Query(template, parameters);
        return Action.optionalSingleResult(tx().execute(insertFriendshipQuery));
    }

    @Override
    public ActionResult resultsForTesting(Record answer) {
        return new ActionResult() {
            {
                put(FriendshipField.FRIEND1_EMAIL, answer.asMap().get("p1." + EMAIL));
                put(FriendshipField.FRIEND2_EMAIL, answer.asMap().get("p2." + EMAIL));
                put(FriendshipField.START_DATE, answer.asMap().get("friendOf." + START_DATE));
            }
        };
    }
}
