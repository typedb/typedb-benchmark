package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.context.DatabaseTransaction;

public abstract class TransactionalAgent<CONTEXT extends DatabaseContext<TRANSACTION>, TRANSACTION extends DatabaseTransaction> extends Agent<CONTEXT> {
    private TRANSACTION tx;

    // TODO Make two types of AutoClosable DB operations - one for opening a read transaction and closing, the other for opening a write transaction and committing
    // TODO however the current flow may also be needed, see EmploymentAgent's allocation for an example

    @Override
    protected void startDbOperation(Action<?, ?> action) {
        this.action = action;
        if (tx == null) {
            tx = backendContext().tx(getSessionKey(), logger(), tracker() + ":" + this.action);
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

    public DbOperation dbOperation(Action<?, ?> action) {
        return new TransactionalDbOperation(action);
    }

    public class TransactionalDbOperation extends DbOperation implements AutoCloseable {

        public TransactionalDbOperation(Action<?, ?> action) {
            startDbOperation(action);
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
