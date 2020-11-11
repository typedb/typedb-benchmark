package grakn.simulation.common.action.read;

import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class CitiesInContinentAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<String>> {
    protected final World.City city;

    public CitiesInContinentAction(DB_OPERATION dbOperation, World.City city) {
        super(dbOperation);
        this.city = city;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city);
    }
}
