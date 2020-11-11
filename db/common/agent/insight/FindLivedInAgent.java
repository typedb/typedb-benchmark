package grakn.simulation.db.common.agent.insight;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.ReadAction;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;

public class FindLivedInAgent<DB_OPERATION extends DbOperation> extends WorldwideInsightAgent<DB_OPERATION> {
    public FindLivedInAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation) {
        return actionFactory().findlivedInAction(dbOperation);
    }
}