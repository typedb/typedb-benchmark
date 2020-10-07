package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.driver.TransactionalDbOperationFactory;
import org.slf4j.Logger;

public class Neo4jOperationFactory extends TransactionalDbOperationFactory<Neo4jTransaction> {
    public Neo4jOperationFactory(Neo4jDriver.Neo4jSession neo4jSession, Logger logger) {
        super(logger);
    }
}
