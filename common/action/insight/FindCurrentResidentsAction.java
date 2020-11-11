package grakn.simulation.common.action.insight;

import grakn.simulation.common.driver.DbOperation;

import java.util.List;

public abstract class FindCurrentResidentsAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, List<String>> {

    public FindCurrentResidentsAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }
}
