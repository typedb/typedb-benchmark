package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertPersonAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.City worldCity;
    protected final LocalDateTime today;
    protected final String email;
    protected final String gender;
    protected final String forename;
    protected final String surname;

    public InsertPersonAction(DB_OPERATION dbOperation, World.City city, LocalDateTime today, String email, String gender, String forename, String surname) {
        super(dbOperation);
        this.worldCity = city;
        this.today = today;
        this.email = email;
        this.gender = gender;
        this.forename = forename;
        this.surname = surname;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, today, email, gender, forename, surname);
    }

    public enum InsertPersonActionField implements ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }
}
