package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.driver.DbOperation;

import java.util.ArrayList;

public abstract class MeanWageOfPeopleInWorldAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, Double> {

    public MeanWageOfPeopleInWorldAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return null;
    }
}
