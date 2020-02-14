package grakn.simulation.common;

import org.slf4j.Logger;

import java.util.function.Function;

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
