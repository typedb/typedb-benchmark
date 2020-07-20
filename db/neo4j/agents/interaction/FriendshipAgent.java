package grakn.simulation.db.neo4j.agents.interaction;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.neo4j.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper.run;

public class FriendshipAgent extends grakn.simulation.db.common.agents.interaction.FriendshipAgent {

    @Override
    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        Neo4jQuery cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        return ExecutorUtils.getOrderedAttribute(tx(), cityResidentsQuery, "resident.email");
    }

    @Override
    protected void insertFriendship(String friend1Email, String friend2Email) {
        String template = "" +
                "MATCH " +
                "(p1:Person {p1Email:$p1Email}),\n" +
                "(p2:Person {p2Email:$p2Email})," +
                "WHERE NOT (p1)-[:FRIEND_OF]-(p2)" +
                "CREATE (p1)-[:FRIEND_OF {startDate: $startDate}]->(p2)"; // TODO this will surely insert duplicates

        Object[] parameters = new Object[]{
                "p1Email", friend1Email,
                "p2Email", friend2Email,
                "startDate", today()
        };
        Neo4jQuery insertFriendshipQuery = new Neo4jQuery(template, parameters);
        log().query("insertFriendship", insertFriendshipQuery);
        run(tx().forNeo4j(), insertFriendshipQuery);
    }
}
