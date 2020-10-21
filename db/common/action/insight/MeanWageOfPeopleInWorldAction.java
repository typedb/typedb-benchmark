package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

public abstract class MeanWageOfPeopleInWorldAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, Double> {

    public MeanWageOfPeopleInWorldAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }
}
