package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertFriendshipAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final LocalDateTime today;
    protected final String friend1Email;
    protected final String friend2Email;

    public InsertFriendshipAction(DB_OPERATION dbOperation, LocalDateTime today, String friend1Email, String friend2Email) {
        super(dbOperation);
        this.today = today;
        this.friend1Email = friend1Email;
        this.friend2Email = friend2Email;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(today, friend1Email, friend2Email);
    }

    public enum InsertFriendshipActionField implements ComparableField {
        FRIEND1_EMAIL, FRIEND2_EMAIL, START_DATE
    }
}
