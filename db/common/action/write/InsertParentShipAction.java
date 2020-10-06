package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.agent.interaction.ParentshipAgent;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class InsertParentShipAction<DB_OPERATION extends DbOperationController.DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final HashMap<ParentshipAgent.SpouseType, String> marriage;
    protected final String childEmail;

    public InsertParentShipAction(DB_OPERATION dbOperation, HashMap<ParentshipAgent.SpouseType, String> marriage, String childEmail) {
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
