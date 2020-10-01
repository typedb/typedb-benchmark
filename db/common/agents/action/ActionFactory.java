package grakn.simulation.db.common.agents.action;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

public abstract class ActionFactory<AGENT_TYPE extends Agent<?>, DB_RETURN_TYPE> {

    protected AGENT_TYPE agent;

    public ActionFactory(AGENT_TYPE agent) {
        this.agent = agent;
    }

    public abstract EmployeeEmailsAction employeeEmailsAction(World.City city, int numEmployments, LocalDateTime earliestDate);

    public abstract CompanyNumbersAction companyNumbersAction(World.Country country, int numCompanies);

    public abstract InsertEmploymentAction<DB_RETURN_TYPE> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);
}
