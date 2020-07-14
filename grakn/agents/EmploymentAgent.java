package grakn.simulation.grakn.agents;

import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.grakn.agents.RelocationAgent.cityResidentsQuery;

public class EmploymentAgent extends grakn.simulation.agents.interaction.EmploymentAgent {

    @Override
    protected List<Long> getCompanyNumbers() {
        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city().country());
        log().query("getEmployeeEmails", companyNumbersQuery);
        int numCompanies = world().getScaleFactor();
        return ExecutorUtils.getOrderedAttribute(tx().forGrakn(), companyNumbersQuery, "company-number", numCompanies);
    }

    @Override
    protected List<String> getEmployeeEmails(LocalDateTime earliestDate) {
        GraqlGet getEmployeeEmailsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getEmployeeEmails", getEmployeeEmailsQuery);
        int numEmployments = world().getScaleFactor();
        return ExecutorUtils.getOrderedAttribute(tx().forGrakn(), getEmployeeEmailsQuery, "email", numEmployments);
    }

    @Override
    protected void insertEmployment(String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours){
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
                        .rel("employment_wage", Graql.var("wage"))
                        .has("start-date", employmentDate),
                Graql.var("wage").isa("wage")
                        .has("wage-value", wageValue)
                        .has("currency", Graql.var("currency")), //TODO Should this be inferred rather than inserted?
                Graql.var("locates").isa("locates")
                        .rel("locates_located", Graql.var("emp"))
                        .rel("locates_location", Graql.var("city")),
                Graql.var("contract").isa("employment-contract")
                        .has("contract-content", contractContent)
                        .has("contracted-hours", contractedHours)
        );
        log().query("insertEmployment", insertEmploymentQuery);
        tx().forGrakn().execute(insertEmploymentQuery);
    }
}
