package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

public abstract class FindSpecificPersonAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, String> {

    protected String PERSON_EMAIL_FOR_QUERY = "person/0/World:Europe:United Kingdom:London/0";

    public FindSpecificPersonAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

}
