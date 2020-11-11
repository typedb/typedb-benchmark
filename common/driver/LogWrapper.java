package grakn.simulation.common.driver;

import org.slf4j.Logger;

public class LogWrapper {
    private final Logger logger;

    public LogWrapper(Logger logger) {
        this.logger = logger;
    }

    public void query(String tracker, String action, Object query) {
        query(tracker, action, query.toString());
    }

    public void query(String tracker, String action, String query) {
        logger.info("({}):{}:\n{}", tracker, action, query);
    }

    public void query(String tracker, Object query) {
        query(tracker, query.toString());
    }

    public void query(String tracker, String query) {
        logger.info("{}:\n{}", tracker, query);
    }
}
