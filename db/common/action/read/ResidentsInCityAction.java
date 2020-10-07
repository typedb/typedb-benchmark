package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class ResidentsInCityAction<DB_OPERATION extends DbOperation> extends ReadAction<DB_OPERATION, List<String>> {

    protected final World.City city;
    protected final int numResidents;
    protected final LocalDateTime earliestDate;

    public ResidentsInCityAction(DB_OPERATION dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation);
        this.city = city;
        this.numResidents = numResidents;
        this.earliestDate = earliestDate;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city, numResidents, earliestDate);
    }
}
