package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.context.LogWrapper;
import org.slf4j.Logger;

import static com.google.common.collect.Iterables.getOnlyElement;

public abstract class DbOperationController {

    private LogWrapper logWrapper;

    DbOperationController(Logger logger) {
        this.logWrapper = new LogWrapper(logger);
    }

    public LogWrapper logger() {
        return logWrapper;
    }

    public abstract ActionFactory<?, ?> actionFactory(); // Get an AgentFactory specific to this DB

    protected abstract void startDbOperation(Action<?, ?> action, String tracker);

    protected abstract void closeDbOperation();

    protected abstract void saveDbOperation();

    public abstract class DbOperation implements AutoCloseable {
        public abstract void close();
        public abstract void commit();
    }

    public abstract DbOperation dbOperation();

    protected abstract DbOperation newDbOperation(Action<?, ?> action, String tracker);
}
