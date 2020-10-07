package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.SpouseType;
import grakn.simulation.db.common.driver.DbOperation;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class InsertParentShipAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final HashMap<SpouseType, String> marriage;
    protected final String childEmail;

    public InsertParentShipAction(DB_OPERATION dbOperation, HashMap<SpouseType, String> marriage, String childEmail) {
        super(dbOperation);
        this.marriage = marriage;
        this.childEmail = childEmail;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(marriage, childEmail);
    }

    public enum InsertParentShipActionField implements ComparableField {
        HUSBAND_EMAIL, WIFE_EMAIL, CHILD_EMAIL
    }
}
