package grakn.simulation.db.common.driver;

import grabl.tracing.client.GrablTracingThreadStatic;

import java.util.function.Supplier;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

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
        if (trace) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(traceName)) {
                return method.get();
            }
        } else {
            return method.get();
        }
    }
}
