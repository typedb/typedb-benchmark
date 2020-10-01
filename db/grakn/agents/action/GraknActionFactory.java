package grakn.simulation.db.grakn.agents.action;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.action.CompanyNumbersAction;
import grakn.simulation.db.common.agents.action.EmployeeEmailsAction;
import grakn.simulation.db.common.agents.action.InsertEmploymentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.agents.interaction.GraknAgent;

import java.time.LocalDateTime;

public class GraknActionFactory extends ActionFactory<GraknAgent, ConceptMap> {
    public GraknActionFactory(GraknAgent agent) {
        super(agent);
    }

    @Override
    public EmployeeEmailsAction employeeEmailsAction(World.City city, int numEmployments, LocalDateTime earliestDate) {
        return null;
    }

    @Override
    public CompanyNumbersAction companyNumbersAction(World.Country country, int numCompanies) {
        return null;
    }

    @Override
    public InsertEmploymentAction<ConceptMap> insertEmploymentAction(World.City city, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        return new GraknInsertEmploymentAction(agent, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }
}
