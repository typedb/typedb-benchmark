package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class InsertEmploymentAction<DB_OPERATION extends Agent<?>.DbOperation, ACTION_RETURN_TYPE> extends Action<DB_OPERATION, ACTION_RETURN_TYPE> {

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
        inputArgs = new ArrayList<Object>() {{
            add(worldCity);
            add(employeeEmail);
            add(companyNumber);
            add(employmentDate);
            add(wageValue);
            add(contractContent);
            add(contractedHours);
        }};
    }

//    @Override
//    protected ArrayList<Object> inputForComparison() {
//        return new ArrayList<Object>() {{
//            add(worldCity);
//            add(employeeEmail);
//            add(companyNumber);
//            add(employmentDate);
//            add(wageValue);
//            add(contractContent);
//            add(contractedHours);
//        }};
//    }
}
