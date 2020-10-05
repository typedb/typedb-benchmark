package grakn.simulation.db.neo4j.agents.interaction;

import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.base.TransactionDbOperationController;
import grakn.simulation.db.neo4j.agents.action.Neo4jActionFactory;
import grakn.simulation.db.neo4j.driver.Neo4jDriver;
import grakn.simulation.db.neo4j.driver.Neo4jTransaction;
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
