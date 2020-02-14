package grakn.simulation.common;

import org.slf4j.Logger;

import java.util.function.Function;

/**
 * A wrapper class to provide common utilities for formatting logs from the Simulation
 * @param <T> The class type that should be used to scope the context of the log
 */
public class LogWrapper<T> {
    private final Logger logger;
    private final Function<T, String> getContext;

    public LogWrapper(Logger logger, Function<T, String> getContext) {
        this.logger = logger;
        this.getContext = getContext;
    }

    public void query(T item, String scope, Object query) {
        if (logger.isInfoEnabled()) {
            logger.info("({}):{}:\n{}", getContext.apply(item), scope, query);
        }
    }

    public void message(T item, String message) {
        if (logger.isInfoEnabled()) {
            logger.info("{}:{}", getContext.apply(item), message);
        }
    }
}
