package grakn.simulation.db.common.driver;

import grakn.simulation.db.common.utils.Trace;

import java.util.function.Supplier;

public abstract class DbOperation implements AutoCloseable {

    protected final String tracker;
    private final boolean trace;

    public DbOperation(String tracker, boolean trace) {
        this.tracker = tracker;
        this.trace = trace;
    }

    public abstract void close();
    public abstract void save();

    protected <T> T trace(Supplier<T> method, String traceName) {
        return Trace.trace(method, traceName, trace);
    }
}
