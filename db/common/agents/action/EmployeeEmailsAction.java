package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public abstract class EmployeeEmailsAction<DB_OPERATION extends DbOperationController.DbOperation> extends Action<DB_OPERATION, List<String>> {

    protected final World.City city;
    protected final int numEmployments;
    protected final LocalDateTime earliestDate;

    public EmployeeEmailsAction(DB_OPERATION dbOperation, World.City city, int numEmployments, LocalDateTime earliestDate) {
        super(dbOperation);
        this.city = city;
        this.numEmployments = numEmployments;
        this.earliestDate = earliestDate;
    }

    @Override
    public HashMap<String, Object> outputForReport(List<String> employeeEmails) {
        return new HashMap<String, Object>(){{
            put("EMPLOYEE_EMAILS", employeeEmails);
        }};
    }
}
