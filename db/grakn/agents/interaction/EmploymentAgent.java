package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.interaction.EmploymentAgentBase;
import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY;
import static grakn.simulation.db.grakn.schema.Schema.COMPANY_NUMBER;
import static grakn.simulation.db.grakn.schema.Schema.CONTRACT;
import static grakn.simulation.db.grakn.schema.Schema.CONTRACTED_HOURS;
import static grakn.simulation.db.grakn.schema.Schema.CONTRACT_CONTENT;
import static grakn.simulation.db.grakn.schema.Schema.COUNTRY;
import static grakn.simulation.db.grakn.schema.Schema.CURRENCY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_CONTRACT;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_EMPLOYEE;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_EMPLOYER;
import static grakn.simulation.db.grakn.schema.Schema.EMPLOYMENT_WAGE;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;
import static grakn.simulation.db.grakn.schema.Schema.WAGE;
import static grakn.simulation.db.grakn.schema.Schema.WAGE_VALUE;

public class EmploymentAgent extends GraknAgent<World.City> implements EmploymentAgentBase {

    @Override
    public List<Long> getCompanyNumbers(World.Country country, String scope, int numCompanies) {
        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(country);
        log().query(scope, companyNumbersQuery);
        return tx().getOrderedAttribute(companyNumbersQuery, COMPANY_NUMBER, numCompanies);
    }

    @Override
    public List<String> getEmployeeEmails(World.City city, String scope, int numEmployments, LocalDateTime earliestDate) {
        GraqlGet getEmployeeEmailsQuery = cityResidentsQuery(city, earliestDate);
        log().query(scope, getEmployeeEmailsQuery);
        return tx().getOrderedAttribute(getEmployeeEmailsQuery, EMAIL, numEmployments);
    }

    @Override
    public void insertEmployment(World.City worldCity, String scope, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours){
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement company = Graql.var(COMPANY);
        Statement country = Graql.var(COUNTRY);
        Statement locationHierarchy = Graql.var(LOCATION_HIERARCHY);
        Statement employment = Graql.var(EMPLOYMENT);
        Statement wage = Graql.var(WAGE);
        Statement locates = Graql.var(LOCATES);
        Statement contract = Graql.var(CONTRACT);
        Statement currency = Graql.var(CURRENCY);

        Statement employeeEmailVar = Graql.var("employee-email").val(employeeEmail);
        Statement companyNumberVar = Graql.var("company-number").val(companyNumber);
        Statement employmentDateVar = Graql.var("employment-date").val(employmentDate);
        Statement wageValueVar = Graql.var("wage-value").val(wageValue);
        Statement contractContentVar = Graql.var("contract-content").val(contractContent);
        Statement contractedHoursVar = Graql.var("contracted-hours").val(contractedHours);

        GraqlInsert insertEmploymentQuery = Graql.match(
                city
                        .isa(CITY)
                        .has(LOCATION_NAME, worldCity.name()),
                person
                        .isa(PERSON)
                        .has(EMAIL, employeeEmailVar),
                company
                        .isa(COMPANY)
                        .has(COMPANY_NUMBER, companyNumberVar),
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
                        .has(START_DATE, employmentDateVar),
                wage
                        .isa(WAGE)
                        .has(WAGE_VALUE, wageValueVar)
                        .has(CURRENCY, currency), //TODO Should this be inferred rather than inserted?
                locates
                        .isa(LOCATES)
                        .rel(LOCATES_LOCATION, employment)
                        .rel(LOCATES_LOCATED, city),
                contract
                        .isa(CONTRACT)
                        .has(CONTRACT_CONTENT, contractContentVar)
                        .has(CONTRACTED_HOURS, contractedHoursVar)
        );
        log().query(scope, insertEmploymentQuery);
        tx().execute(insertEmploymentQuery);
    }

//    protected int checkCount() {
//        Statement city = Graql.var(CITY);
//        Statement person = Graql.var(PERSON);
//        Statement company = Graql.var(COMPANY);
//        Statement country = Graql.var(COUNTRY);
//        Statement locationHierarchy = Graql.var(LOCATION_HIERARCHY);
//        Statement employment = Graql.var(EMPLOYMENT);
//        Statement wage = Graql.var(WAGE);
//        Statement locates = Graql.var(LOCATES);
//        Statement contract = Graql.var(CONTRACT);
//        Statement currency = Graql.var(CURRENCY);
//
//        Statement employeeEmailVar = Graql.var("employee-email");
//        Statement companyNumberVar = Graql.var("company-number");
//        Statement employmentDateVar = Graql.var("employment-date");
//        Statement wageValueVar = Graql.var("wage-value");
//        Statement contractContentVar = Graql.var("contract-content");
//        Statement contractedHoursVar = Graql.var("contracted-hours");
//
//        GraqlGet.Aggregate countQuery = Graql.match(
//                        city
//                                .isa(CITY)
//                                .has(LOCATION_NAME, city().name()),
//                        person
//                                .isa(PERSON)
//                                .has(EMAIL, employeeEmailVar),
//                        company
//                                .isa(COMPANY)
//                                .has(COMPANY_NUMBER, companyNumberVar),
//                        country
//                                .isa(COUNTRY)
//                                .has(CURRENCY, currency),
//                        locationHierarchy
//                                .isa(LOCATION_HIERARCHY)
//                                .rel(city)
//                                .rel(country),
//                        employment
//                                .isa(EMPLOYMENT)
//                                .rel(EMPLOYMENT_EMPLOYEE, person)
//                                .rel(EMPLOYMENT_EMPLOYER, company)
//                                .rel(EMPLOYMENT_CONTRACT, contract)
//                                .rel(EMPLOYMENT_WAGE, wage)
//                                .has(START_DATE, employmentDateVar),
//                        wage
//                                .isa(WAGE)
//                                .has(WAGE_VALUE, wageValueVar)
//                                .has(CURRENCY, currency),
//                        locates
//                                .isa(LOCATES)
//                                .rel(LOCATES_LOCATION, employment)
//                                .rel(LOCATES_LOCATED, city),
//                        contract
//                                .isa(CONTRACT)
//                                .has(CONTRACT_CONTENT, contractContentVar)
//                                .has(CONTRACTED_HOURS, contractedHoursVar)
//        ).get().count();
//        log().query("checkCount", countQuery);
//        return tx().count(countQuery);
//    }
}
