package grakn.simulation.neo4j.action.insight;

import grakn.simulation.common.action.insight.ArbitraryOneHopAction;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.List;

public class Neo4jArbitraryOneHopAction extends ArbitraryOneHopAction<Neo4jOperation> {
    public Neo4jArbitraryOneHopAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Integer run() {
        List<Record> results = dbOperation.execute(new Query(query()));
        return null;
    }

    public static String query() {
//        TODO Needs to do inference to be equivalent to Grakn
        return "MATCH (person:Person {email: \"" + PERSON_EMAIL_FOR_QUERY + "\"})--(x)\n" +
                "RETURN x";
    }
}
