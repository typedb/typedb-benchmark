package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.ArbitraryOneHopAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.List;

public class Neo4jArbitraryOneHopAction extends ArbitraryOneHopAction<Neo4jOperation> {
    public Neo4jArbitraryOneHopAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Integer run() {
        String query = "" +
                "MATCH (person:Person {email: \"" + PERSON_EMAIL_FOR_QUERY + "\"})--(x)" +
                "RETURN x";
        List<Record> results = dbOperation.execute(new Query(query));
        return null;
    }
}
