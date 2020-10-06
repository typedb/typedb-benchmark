package grakn.simulation.db.common.operation;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbTransaction;
import grakn.simulation.db.common.driver.TransactionalDbDriver;
import org.slf4j.Logger;

public abstract class TransactionDbOperationController<TRANSACTION extends DbTransaction> extends DbOperationController {
    private final TransactionalDbDriver<TRANSACTION, ?>.Session session;
    private TRANSACTION tx;
    private TransactionalDbOperation dbOperation;

    public TransactionDbOperationController(TransactionalDbDriver<TRANSACTION, ?>.Session session, Logger logger) {
        super(logger);
        this.session = session;
    }

    @Override
    protected void startDbOperation(String actionName, String tracker) {
        if (tx == null) {
            tx = session.tx(logger(), tracker + ":" + actionName);
        }
    }

    @Override
    protected void closeDbOperation() {
        tx.close();
        tx = null;
    }

    @Override
    protected void saveDbOperation() {
        tx.commit();
        tx = null;
    }

    public TransactionalDbOperation dbOperation() {
        return dbOperation;
    }

    public DbOperation newDbOperation(Action<?, ?> action, String tracker) {
        dbOperation = new TransactionalDbOperation(action, tracker);
        return dbOperation;
    }

    public DbOperation newDbOperation(String actionName, String tracker) {
        dbOperation = new TransactionalDbOperation(actionName, tracker);
        return dbOperation;
    }

    public class TransactionalDbOperation extends DbOperation implements AutoCloseable {

        public TransactionalDbOperation(Action<?, ?> action, String tracker) {
            startDbOperation(action.name(), tracker);
        }

        public TransactionalDbOperation(String actionName, String tracker) {
            startDbOperation(actionName, tracker);
        }

        @Override
        public void save() {
            saveDbOperation();
        }

        @Override
        public void close() {
            closeDbOperation();
        }

        public TRANSACTION tx() {
            return tx;
        }
    }
}
