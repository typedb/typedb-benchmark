package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.context.TransactionalDbDriver;
import grakn.simulation.db.common.context.DbTransaction;
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
    protected void startDbOperation(Action<?, ?> action, String tracker) {
        if (tx == null) {
            tx = session.tx(logger(), tracker + ":" + action.name());
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

    public class TransactionalDbOperation extends DbOperation implements AutoCloseable {

        public TransactionalDbOperation(Action<?, ?> action, String tracker) {
            startDbOperation(action, tracker);
        }

        @Override
        public void commit() {
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
