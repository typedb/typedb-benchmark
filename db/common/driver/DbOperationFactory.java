package grakn.simulation.db.common.driver;

import org.slf4j.Logger;

public abstract class DbOperationFactory<DB_OPERATION extends DbOperation> {

    private final LogWrapper logWrapper;

    DbOperationFactory(Logger logger) {
        this.logWrapper = new LogWrapper(logger);
    }

    public LogWrapper logger() {
        return logWrapper;
    }

    public abstract DB_OPERATION dbOperation();

    public abstract DB_OPERATION newDbOperation(String tracker, boolean trace);
}
