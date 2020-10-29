package grakn.simulation.db.common.agent.insight;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.ReadAction;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;

public class FourHopAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends WorldwideInsightAgent<DB_DRIVER, DB_OPERATION> {
    public FourHopAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected ReadAction<DB_OPERATION, ?> getAction(DB_OPERATION dbOperation) {
        return actionFactory().fourHopAction(dbOperation);
    }
}
