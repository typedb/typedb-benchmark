package grakn.simulation.db.common.driver;

public abstract class DbOperation implements AutoCloseable {

    protected final String tracker;

    public DbOperation(String tracker) {
        this.tracker = tracker;
    }

    public abstract void close();
    public abstract void save();
}
