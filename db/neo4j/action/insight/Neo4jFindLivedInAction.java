package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.common.action.insight.FindLivedInAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;

public class Neo4jFindLivedInAction extends FindLivedInAction<Neo4jOperation> {
    public Neo4jFindLivedInAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(new Query(query()), "person.email", null);
    }

    public static String query() {
        return "MATCH (person:Person)-[livedIn]->(city:City {locationName: \"Berlin\"})\n" +
                "WHERE TYPE(livedIn) = \"BORN_IN\" OR TYPE(livedIn) = \"RELOCATED_TO\"\n" +
                "RETURN person.email";
    }
}
