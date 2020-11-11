package grakn.simulation.common.action.write;

import grakn.simulation.common.action.Action;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertEmploymentAction<DB_OPERATION extends DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {

    protected final World.City worldCity;
    protected final String employeeEmail;
    protected final long companyNumber;
    protected final LocalDateTime employmentDate;
    protected final double wageValue;
    protected final String contractContent;
    protected final double contractedHours;

    public InsertEmploymentAction(DB_OPERATION dbOperation, World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(dbOperation);
        this.worldCity = city;
        this.employeeEmail = employeeEmail;
        this.companyNumber = companyNumber;
        this.employmentDate = employmentDate;
        this.wageValue = wageValue;
        this.contractContent = contractContent;
        this.contractedHours = contractedHours;
    }

    @Override
    protected ArrayList<Object> inputForReport() {
        return argsList(worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    public enum InsertEmploymentActionField implements ComparableField {
        CITY_NAME, PERSON_EMAIL, COMPANY_NUMBER, START_DATE, WAGE, CURRENCY, CONTRACT_CONTENT, CONTRACTED_HOURS
    }
}
