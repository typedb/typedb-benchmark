package grakn.simulation.db.neo4j.driver;

import grakn.simulation.db.common.driver.TransactionalDbOperationFactory;
import org.neo4j.driver.Session;
import org.slf4j.Logger;

public class Neo4jOperationFactory extends TransactionalDbOperationFactory<Neo4jOperation> {
    private final Session session;

    public Neo4jOperationFactory(org.neo4j.driver.Session session, Logger logger) {
        super(logger);
        this.session = session;
    }

    @Override
    public Neo4jOperation newDbOperation(String tracker, boolean trace) {
        return new Neo4jOperation(session, logger(), tracker, trace);
    }
}
