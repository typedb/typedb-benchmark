package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;

import java.util.HashMap;

public abstract class ReadAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    public ReadAction(DB_OPERATION dbOperation) {
        super(dbOperation);
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(ACTION_RETURN_TYPE answer) {
        return new HashMap<ComparableField, Object>() {{
            put(ReadActionField.SINGLE_FIELD_ANSWER, answer);
        }};
    }

    public enum ReadActionField implements ComparableField {
        SINGLE_FIELD_ANSWER
    }
}
