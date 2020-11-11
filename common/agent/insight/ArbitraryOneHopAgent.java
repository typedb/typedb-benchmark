package grakn.simulation.common.agent.insight;

import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.read.ReadAction;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;

public class ArbitraryOneHopAgent<DB_OPERATION extends DbOperation> extends WorldwideInsightAgent<DB_OPERATION> {
    public ArbitraryOneHopAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation) {
        return actionFactory().arbitraryOneHopAction(dbOperation);
    }
}
