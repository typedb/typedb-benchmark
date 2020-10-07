package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertRelocationAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.City city;
    protected final LocalDateTime today;
    protected final String relocateeEmail;
    protected final String relocationCityName;

    public InsertRelocationAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(dbOperation);
        this.city = city;
        this.today = today;
        this.relocateeEmail = relocateeEmail;
        this.relocationCityName = relocationCityName;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(city, today, relocateeEmail, relocationCityName);
    }

    public enum InsertRelocationActionField implements ComparableField {
        PERSON_EMAIL, OLD_CITY_NAME, NEW_CITY_NAME, RELOCATION_DATE
    }
}
