package grakn.simulation.db.common.driver;

public abstract class TransactionalDbOperation extends DbOperation implements AutoCloseable {
    public TransactionalDbOperation(String tracker) {
        super(tracker);
    }
}
