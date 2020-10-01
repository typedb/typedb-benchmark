package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.action.CompanyNumbersAction;
import grakn.simulation.db.common.agents.action.EmployeeEmailsAction;
import grakn.simulation.db.common.agents.action.InsertEmploymentAction;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.common.agents.utils.Allocation.allocate;

public interface EmploymentAgentBase extends InteractionAgent<World.City> {

    double MIN_ANNUAL_WAGE = 18000.00;
    double MAX_ANNUAL_WAGE = 80000.00;
    double MIN_CONTRACTED_HOURS = 30.0;
    double MAX_CONTRACTED_HOURS = 70.0;
    int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    int MAX_CONTRACT_CHARACTER_LENGTH = 600;

    @Override
    default void iterate(Agent<?> agent, World.City city, SimulationContext simulationContext) {
        LocalDateTime employmentDate = simulationContext.today().minusYears(0);
        List<String> employeeEmails;
        List<Long> companyNumbers;

        EmployeeEmailsAction employeeEmailsAction = agent.actionFactory().employeeEmailsAction(city, simulationContext.world().getScaleFactor(), employmentDate);
        try(Agent<?>.DbOperation dbOperation = agent.dbOperation(employeeEmailsAction)) {
            employeeEmails = agent.runAction(employeeEmailsAction);
        }

        CompanyNumbersAction companyNumbersAction = agent.actionFactory().companyNumbersAction(city.country(), simulationContext.world().getScaleFactor());
        try(Agent<?>.DbOperation dbOperation = agent.dbOperation(employeeEmailsAction)) {
            companyNumbers = agent.runAction(companyNumbersAction);
        }

        try(Agent<?>.DbOperation dbOperation = agent.dbOperation(employeeEmailsAction)) {
            // A second transaction is being used to circumvent graknlabs/grakn issue #5585
            boolean allocated = allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
                double wageValue = agent.randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
                String contractContent = agent.randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
                double contractedHours = agent.randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
                InsertEmploymentAction<?> insertEmploymentAction = agent.actionFactory().insertEmploymentAction(city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
                agent.runAction(insertEmploymentAction);
            });
            if (allocated) {
                dbOperation.commit();
            }
        }
    }
}
