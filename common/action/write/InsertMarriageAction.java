package grakn.simulation.common.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.util.ArrayList;

public abstract class InsertMarriageAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {
    protected final World.City worldCity;
    protected final int marriageIdentifier;
    protected final String wifeEmail;
    protected final String husbandEmail;

    public InsertMarriageAction(DB_OPERATION dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(dbOperation);
        this.worldCity = city;
        this.marriageIdentifier = marriageIdentifier;
        this.wifeEmail = wifeEmail;
        this.husbandEmail = husbandEmail;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, marriageIdentifier, wifeEmail, husbandEmail);
    }
    public enum InsertMarriageActionField implements ComparableField {
        MARRIAGE_IDENTIFIER, WIFE_EMAIL, HUSBAND_EMAIL, CITY_NAME
    }

}
