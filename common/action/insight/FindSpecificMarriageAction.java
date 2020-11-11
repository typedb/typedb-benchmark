package grakn.simulation.common.action.insight;

import grakn.simulation.common.driver.DbOperation;

public abstract class FindSpecificMarriageAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, String> {

    protected static int MARRIAGE_ID_FOR_QUERY = "3/Australasia:Australia:Canberra/0".hashCode();

    public FindSpecificMarriageAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }
}
