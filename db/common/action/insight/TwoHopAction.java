package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

import java.util.List;

public abstract class TwoHopAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, List<String>> {

    public TwoHopAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

}
