package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.neo4j.action.Neo4jActionFactory;
import org.slf4j.Logger;

public class Neo4jDbOperationController extends TransactionDbOperationController<Neo4jTransaction> {
    public Neo4jDbOperationController(Neo4jDriver.Neo4jSession neo4jSession, Logger logger) {
        super(neo4jSession, logger);
    }

    @Override
    public ActionFactory<?, ?> actionFactory() {
        return new Neo4jActionFactory(this);
    }
}
