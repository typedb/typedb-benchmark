package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

public abstract class FindSpecificPersonAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, String> {

    protected static String PERSON_EMAIL_FOR_QUERY = "email/1/Europe:United Kingdom:London/0";

    public FindSpecificPersonAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }
}
