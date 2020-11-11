package grakn.simulation.db.common.driver;

import org.slf4j.Logger;


public abstract class TransactionalDbOperationFactory<DB_OPERATION extends TransactionalDbOperation> extends DbOperationFactory<DB_OPERATION> {

    public TransactionalDbOperationFactory(Logger logger) {
        super(logger);
    }

    public DB_OPERATION dbOperation() { // Needed for now but will disappear once agents give the operations to the actions rather than the ActionFactory
        return null;
    }

    @Override
    public abstract DB_OPERATION newDbOperation(String tracker, boolean trace);
}
