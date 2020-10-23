package grakn.simulation.db.common.action.insight;

import grakn.simulation.db.common.driver.DbOperation;

public abstract class ArbitraryOneHopAction<DB_OPERATION extends DbOperation> extends WorldwideInsightAction<DB_OPERATION, Integer> {

    protected static String PERSON_EMAIL_FOR_QUERY = "email/0/World:Europe:United Kingdom:London/0";

    public ArbitraryOneHopAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

}
