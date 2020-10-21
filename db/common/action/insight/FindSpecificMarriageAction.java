package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

public abstract class FindSpecificMarriageAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, String> {

    protected String MARRIAGE_ID_FOR_QUERY = "marriage/0/World:Europe:United Kingdom:London/0";

    public FindSpecificMarriageAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

}
