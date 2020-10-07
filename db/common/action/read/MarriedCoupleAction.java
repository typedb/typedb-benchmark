package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.SpouseType;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MarriedCoupleAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<HashMap<SpouseType, String>>> {
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
