package grakn.simulation.db.common.agents.base;

import org.slf4j.Logger;

public class LogWrapper {
    private final Logger logger;

    LogWrapper(Logger logger) {
        this.logger = logger;
    }

    public void query(String tracker, String scope, Object query) {
        query(tracker, scope, query.toString());
    }

    public void query(String tracker, String scope, String query) {
        logger.info("({}):{}:\n{}", tracker, scope, query);
    }

    public void message(String tracker, String scope, String message) {
        logger.info("({}):{}:\n{}", tracker, scope, message);
    }
}
