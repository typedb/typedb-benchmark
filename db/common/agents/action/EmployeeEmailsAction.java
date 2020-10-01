package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

public abstract class EmployeeEmailsAction extends Action<List<String>> {

    protected final World.City city;
    protected final int numEmployments;
    protected final LocalDateTime earliestDate;

    public EmployeeEmailsAction(World.City city, int numEmployments, LocalDateTime earliestDate) {
        super(dbOperation);
        this.city = city;
        this.numEmployments = numEmployments;
        this.earliestDate = earliestDate;
    }

    @Override
    public Report resultsForTesting(List<String> employeeEmails) {
        return new Report(){{
            put("EMPLOYEE_EMAILS", employeeEmails);
        }};
    }
}
