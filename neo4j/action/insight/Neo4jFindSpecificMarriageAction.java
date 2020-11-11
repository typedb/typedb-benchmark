package grakn.simulation.neo4j.action.insight;

import grakn.simulation.common.action.insight.FindSpecificMarriageAction;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.stream.Collectors;

public class Neo4jFindSpecificMarriageAction extends FindSpecificMarriageAction<Neo4jOperation> {
    public Neo4jFindSpecificMarriageAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        return optionalSingleResult(dbOperation.execute(new Query(query())).stream().map(ans -> ans.get("marriedTo.marriageId").asString()).collect(Collectors.toList()));
    }

    public static String query() {
        return "MATCH ()-[marriedTo:MARRIED_TO {marriageId: \"" + MARRIAGE_ID_FOR_QUERY + "\"}]-()\n" +
                "RETURN marriedTo.marriageId";
    }
}
