package grakn.simulation.db.neo4j.action.insight;

import grakn.simulation.db.common.action.insight.FindTransactionCurrencyAction;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.List;
import java.util.stream.Collectors;

public class Neo4jFindTransactionCurrencyAction extends FindTransactionCurrencyAction<Neo4jOperation> {
    public Neo4jFindTransactionCurrencyAction(Neo4jOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.execute(new Query(query())).stream().map(ans -> ans.get("transaction.currency").asString()).collect(Collectors.toList());
    }

    public static String query() {
        return "" +
                "MATCH (transaction:Transaction), (country:Country {locationName: transaction.locationName})" +
                "RETURN transaction.currency";
    }
}
