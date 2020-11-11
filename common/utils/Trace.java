package grakn.simulation.common.utils;

import grabl.tracing.client.GrablTracingThreadStatic;

import java.util.function.Supplier;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public class Trace {

    /**
     * A wrapper to trace a method or not according to a supplied boolean
     */
    public static <T> T trace(Supplier<T> methodToTrace, String traceName, boolean trace) {
        if (trace) {
            try (GrablTracingThreadStatic.ThreadTrace threadTrace = traceOnThread(traceName)) {
                return methodToTrace.get();
            }
        } else {
            return methodToTrace.get();
        }
    }
}
