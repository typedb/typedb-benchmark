package grakn.simulation.common.action.insight;

import grakn.simulation.common.action.read.ReadAction;
import grakn.simulation.common.driver.DbOperation;

import java.util.ArrayList;

public abstract class WorldwideInsightAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends ReadAction<DB_OPERATION, ACTION_RETURN_TYPE> {

    public WorldwideInsightAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return new ArrayList<>();
    }
}
