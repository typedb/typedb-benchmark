package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class UpdateAgesOfPeopleInCityAction<DB_OPERATION extends DbOperationController.DbOperation> extends ReadAction<DB_OPERATION, Integer> {
    protected final LocalDateTime today;
    protected final World.City city;

    public UpdateAgesOfPeopleInCityAction(DB_OPERATION dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation);
        this.today = today;
        this.city = city;
    }

    @Override
    public HashMap<ComparableField, Object> outputForReport(Integer answer) {
        return new HashMap<>(); // Nothing to report for this action
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(today, city);
    }
}
