package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.agent.interaction.ParentshipAgent;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MarriedCoupleAction<DB_OPERATION extends DbOperationController.DbOperation> extends ReadAction<DB_OPERATION, List<HashMap<ParentshipAgent.SpouseType, String>>> {
    protected final World.City city;
    protected final LocalDateTime today;

    public MarriedCoupleAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation);
        this.city = city;
        this.today = today;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return Action.argsList(city, today);
    }
}
