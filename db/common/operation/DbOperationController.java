package grakn.simulation.db.common.operation;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.ActionFactory;
import org.slf4j.Logger;

public abstract class DbOperationController {

    private final LogWrapper logWrapper;

    DbOperationController(Logger logger) {
        this.logWrapper = new LogWrapper(logger);
    }

    public LogWrapper logger() {
        return logWrapper;
    }

    public abstract ActionFactory<?, ?> actionFactory(); // Get an AgentFactory specific to this DB

    protected abstract void startDbOperation(String actionName, String tracker);

    protected abstract void closeDbOperation();

    protected abstract void saveDbOperation();

    public abstract class DbOperation implements AutoCloseable {
        public abstract void close();
        public abstract void save();
    }

    public abstract DbOperation dbOperation();

    public abstract DbOperation newDbOperation(Action<?, ?> action, String tracker);

    public abstract DbOperation newDbOperation(String actionName, String tracker);
}
