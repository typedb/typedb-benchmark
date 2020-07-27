package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.common.agents.utils.Allocation.allocate;

public abstract class EmploymentAgent extends CityAgent {

    private static final double MIN_ANNUAL_WAGE = 18000.00;
    private static final double MAX_ANNUAL_WAGE = 80000.00;
    private static final double MIN_CONTRACTED_HOURS = 30.0;
    private static final double MAX_CONTRACTED_HOURS = 70.0;
    private static final int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    private static final int MAX_CONTRACT_CHARACTER_LENGTH = 600;

    private LocalDateTime employmentDate;

    @Override
    public final void iterate() {
        employmentDate = today().minusYears(2);

        List<String> employeeEmails;
        List<Long> companyNumbers;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getEmployeeEmails"))) {
            employeeEmails = getEmployeeEmails(employmentDate);
        }
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getCompanyNumbers"))) {
            companyNumbers = getCompanyNumbers();
        }
        tx().commitWithTracing();
        closeTx();
        // A second transaction is being used to circumvent graknlabs/grakn issue #5585
        allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
            double wageValue = randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
            String contractContent = randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
            double contractedHours = randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertEmployment"))) {
                insertEmployment(employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
            }
        });
        tx().commitWithTracing();
    }

    protected abstract List<Long> getCompanyNumbers();

    protected abstract List<String> getEmployeeEmails(LocalDateTime earliestDate);

    protected abstract void insertEmployment(String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours);
}
