package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.agents.utils.Allocation.allocate;

public interface EmploymentAgentBase extends InteractionAgent<World.City> {

    double MIN_ANNUAL_WAGE = 18000.00;
    double MAX_ANNUAL_WAGE = 80000.00;
    double MIN_CONTRACTED_HOURS = 30.0;
    double MAX_CONTRACTED_HOURS = 70.0;
    int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    int MAX_CONTRACT_CHARACTER_LENGTH = 600;


    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        LocalDateTime employmentDate = iterationContext.today().minusYears(2);

        int numEmployments = iterationContext.world().getScaleFactor();
        List<String> employeeEmails;
        List<Long> companyNumbers;
        agent.startAction();
        String scope1 = "getEmployeeEmails";
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace(scope1))) {
            employeeEmails = getEmployeeEmails(city, scope1, numEmployments, employmentDate);
        }

        int numCompanies = iterationContext.world().getScaleFactor();
        String scope2 = "getCompanyNumbers";
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace(scope2))) {
            companyNumbers = getCompanyNumbers(city.country(), scope2, numCompanies);
        }
        agent.commitAction();  //TODO Should be close not commit?
        agent.startAction();
        // A second transaction is being used to circumvent graknlabs/grakn issue #5585
        boolean allocated = allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
            String scope3 = "insertEmployment";
            double wageValue = agent.randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
            String contractContent = agent.randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
            double contractedHours = agent.randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
            try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace(scope3))) {
                insertEmployment(city, scope3, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
            }
        });
        if (allocated) {
            agent.commitAction();
        }
        return null;
    }

    List<Long> getCompanyNumbers(World.Country country, String scope, int numCompanies);

    List<String> getEmployeeEmails(World.City city, String scope, int numEmployments, LocalDateTime earliestDate);

    void insertEmployment(World.City city, String scope, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);

}
