package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.grakn.agents.interaction.Schema.CITY;
import static grakn.simulation.db.grakn.agents.interaction.Schema.COMPANY;
import static grakn.simulation.db.grakn.agents.interaction.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.agents.interaction.Schema.CONTRACTED_HOURS;
import static grakn.simulation.db.grakn.agents.interaction.Schema.CONTRACT_CONTENT;
import static grakn.simulation.db.grakn.agents.interaction.Schema.COUNTRY;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMAIL;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMPLOYMENT;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMPLOYMENT_CONTRACT;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMPLOYMENT_EMPLOYEE;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMPLOYMENT_EMPLOYER;
import static grakn.simulation.db.grakn.agents.interaction.Schema.EMPLOYMENT_WAGE;
import static grakn.simulation.db.grakn.agents.interaction.Schema.LOCATES;
import static grakn.simulation.db.grakn.agents.interaction.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.agents.interaction.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.agents.interaction.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.agents.interaction.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.agents.interaction.Schema.PERSON;
import static grakn.simulation.db.grakn.agents.interaction.Schema.CURRENCY;
import static grakn.simulation.db.grakn.agents.interaction.Schema.START_DATE;
import static grakn.simulation.db.grakn.agents.interaction.Schema.WAGE;
import static grakn.simulation.db.grakn.agents.interaction.Schema.WAGE_VALUE;

public class EmploymentAgent extends grakn.simulation.db.common.agents.interaction.EmploymentAgent {

    @Override
    protected List<Long> getCompanyNumbers() {
        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city().country());
        log().query("getCompanyNumbers", companyNumbersQuery);
        int numCompanies = world().getScaleFactor();
        return ((Transaction)tx()).getOrderedAttribute(companyNumbersQuery, "company-number", numCompanies);
    }

    @Override
    protected List<String> getEmployeeEmails(LocalDateTime earliestDate) {
        GraqlGet getEmployeeEmailsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getEmployeeEmails", getEmployeeEmailsQuery);
        int numEmployments = world().getScaleFactor();
        return ((Transaction)tx()).getOrderedAttribute(getEmployeeEmailsQuery, "email", numEmployments);
    }

    @Override
    protected void insertEmployment(String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours){
        Statement city = Graql.var("city");
        Statement person = Graql.var("person");
        Statement company = Graql.var("company");
        Statement country = Graql.var("country");
        Statement locationHierarchy = Graql.var("lh");
        Statement employment = Graql.var("emp");
        Statement wage = Graql.var("wage");
        Statement locates = Graql.var("locates");
        Statement contract = Graql.var("contract");
        Statement currency = Graql.var("currency");

        GraqlInsert insertEmploymentQuery = Graql.match(
                city
                        .isa(CITY)
                        .has(LOCATION_NAME, city().name()),
                person
                        .isa(PERSON)
                        .has(EMAIL, employeeEmail),
                company
                        .isa(COMPANY)
                        .has(COMPANY_NUMBER, companyNumber),
                country
                        .isa(COUNTRY)
                        .has(CURRENCY, currency),
                locationHierarchy
                        .isa(LOCATION_HIERARCHY)
                        .rel(city)
                        .rel(country)
        ).insert(
                employment
                        .isa(EMPLOYMENT)
                        .rel(EMPLOYMENT_EMPLOYEE, person)
                        .rel(EMPLOYMENT_EMPLOYER, company)
                        .rel(EMPLOYMENT_CONTRACT, contract)
                        .rel(EMPLOYMENT_WAGE, wage)
                        .has(START_DATE, employmentDate),
                wage
                        .isa(WAGE)
                        .has(WAGE_VALUE, wageValue)
                        .has(CURRENCY, currency), //TODO Should this be inferred rather than inserted?
                locates
                        .isa(LOCATES)
                        .rel(LOCATES_LOCATION, employment)
                        .rel(LOCATES_LOCATED, city),
                contract
                        .isa(EMPLOYMENT_CONTRACT)
                        .has(CONTRACT_CONTENT, contractContent)
                        .has(CONTRACTED_HOURS, contractedHours)
        );
        log().query("insertEmployment", insertEmploymentQuery);
        tx().forGrakn().execute(insertEmploymentQuery);
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(

        ).get().count();
        return tx().forGrakn().execute(countQuery).get().get(0).number().intValue();
    }
}
