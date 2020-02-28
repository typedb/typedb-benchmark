package grakn.simulation.agents;

import grakn.simulation.agents.common.CityAgent;
import grakn.simulation.common.ExecutorUtils;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.agents.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.common.Allocation.allocate;

public class EmploymentAgent extends CityAgent {

    private static final int NUM_EMPLOYMENTS = 5;
    private static final int NUM_COMPANIES = 5;
    private static final double MIN_ANNUAL_WAGE = 18000.00;
    private static final double MAX_ANNUAL_WAGE = 80000.00;
    private static final double MIN_CONTRACTED_HOURS = 30.0;
    private static final double MAX_CONTRACTED_HOURS = 70.0;
    private static final int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    private static final int MAX_CONTRACT_CHARACTER_LENGTH = 600;

    private LocalDateTime employmentDate;

    @Override
    public void iterate() {
        employmentDate = today().minusYears(2);

        List<String> employeeEmails;
        List<Long> companyNumbers;
        employeeEmails = getEmployeeEmails(employmentDate);
        companyNumbers = getCompanyNumbers();
        tx().commit();
        closeTx();
        // A second transaction is being used to circumvent graknlabs/grakn issue #5585
        allocate(employeeEmails, companyNumbers, this::insertEmployment);
        tx().commit();
    }

    private List<Long> getCompanyNumbers() {
        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city().country());
        log().query("getEmployeeEmails", companyNumbersQuery);
        return ExecutorUtils.getOrderedAttribute(tx(), companyNumbersQuery, "company-number", NUM_COMPANIES);
    }

    private List<String> getEmployeeEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered getEmployeeEmailsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getEmployeeEmails", getEmployeeEmailsQuery);
        return ExecutorUtils.getOrderedAttribute(tx(), getEmployeeEmailsQuery, "email", NUM_EMPLOYMENTS);
    }

    private void insertEmployment(String employeeEmail, Long companyNumber){
        GraqlInsert insertEmploymentQuery = Graql.match(
                Graql.var("city")
                        .isa("city")
                        .has("location-name", city().name()),
                Graql.var("p")
                        .isa("person")
                        .has("email", employeeEmail),
                Graql.var("company")
                        .isa("company")
                        .has("company-number", companyNumber),
                Graql.var("country")
                        .isa("country")
                        .has("currency", Graql.var("currency")),
                Graql.var("lh")
                        .isa("location-hierarchy")
                        .rel(Graql.var("city"))
                        .rel(Graql.var("country"))
        ).insert(
                Graql.var("emp").isa("employment")
                        .rel("employment_employee", Graql.var("p"))
                        .rel("employment_employer", Graql.var("company"))
                        .rel("employment_contract", Graql.var("contract"))
                        .has("start-date", employmentDate)
                        .has("annual-wage", randomAttributeGenerator().boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE), Graql.var("r")),
                Graql.var("r").has("currency", Graql.var("currency")), //TODO Should this be inferred rather than inserted?
                Graql.var("locates").isa("locates")
                        .rel("locates_located", Graql.var("emp"))
                        .rel("locates_location", Graql.var("city")),
                Graql.var("contract").isa("employment-contract")
                        .has("contract-content", randomAttributeGenerator().boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH))
                        .has("contracted-hours", randomAttributeGenerator().boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS))
        );
        log().query("insertEmployment", insertEmploymentQuery);
        tx().execute(insertEmploymentQuery);
    }
}
