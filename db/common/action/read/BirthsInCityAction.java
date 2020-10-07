package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class BirthsInCityAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<String>> {
    protected final World.City worldCity;
    protected final LocalDateTime today;

    public BirthsInCityAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation);
        this.worldCity = city;
        this.today = today;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, today);
    }
}
