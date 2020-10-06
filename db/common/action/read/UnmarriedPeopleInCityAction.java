package grakn.simulation.db.common.action.read;

import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class UnmarriedPeopleInCityAction<DB_OPERATION extends DbOperationController.DbOperation> extends ReadAction<DB_OPERATION, List<String>> {
    protected final World.City city;
    protected final String gender;
    protected final LocalDateTime dobOfAdults;

    public UnmarriedPeopleInCityAction(DB_OPERATION dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(dbOperation);
        this.city = city;
        this.gender = gender;
        this.dobOfAdults = dobOfAdults;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city, gender, dobOfAdults);
    }
}
