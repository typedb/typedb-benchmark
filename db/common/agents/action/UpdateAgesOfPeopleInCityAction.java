package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.util.HashMap;

public abstract class UpdateAgesOfPeopleInCityAction<DB_OPERATION extends DbOperationController.DbOperation> extends Action<DB_OPERATION, Integer> {
    protected final World.City city;

    public UpdateAgesOfPeopleInCityAction(DB_OPERATION dbOperation, World.City city) {
        super(dbOperation);
        this.city = city;
    }

    @Override
    public HashMap<String, Object> outputForReport(Integer answer) {
        return new HashMap<>(); // Nothing to report for this action
    }
}
