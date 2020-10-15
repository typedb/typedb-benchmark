package grakn.simulation.db.common.action.write;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertCompanyAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {

    protected final World.Country country;
    protected final LocalDateTime today;
    protected final int companyNumber;
    protected final String companyName;

    public InsertCompanyAction(DB_OPERATION dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation);
        this.country = country;
        this.today = today;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(country, today, companyNumber, companyName);
    }

    public enum InsertCompanyActionField implements ComparableField {
        COMPANY_NUMBER, COMPANY_NAME, DATE_OF_INCORPORATION, COUNTRY
    }
}
