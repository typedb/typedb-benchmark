package grakn.simulation.db.grakn.action.write;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.write.InsertEmploymentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.HashMap;

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

public class GraknInsertEmploymentAction extends InsertEmploymentAction<GraknOperation, ConceptMap> {
    public GraknInsertEmploymentAction(GraknOperation dbOperation, World.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(dbOperation, worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public ConceptMap run() {
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

        GraqlInsert insertEmploymentQuery = Graql.match(
                city
                        .isa(CITY)
                        .has(LOCATION_NAME, worldCity.name()),
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
                        .rel(LOCATES_LOCATION, city)
                        .rel(LOCATES_LOCATED, employment),
                contract
                        .isa(CONTRACT)
                        .has(CONTRACT_CONTENT, contractContent)
                        .has(CONTRACTED_HOURS, contractedHours)
        );
        return singleResult(dbOperation.execute(insertEmploymentQuery));
    }

    @Override
    public HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
        return new HashMap<ComparableField, Object>() {{
                put(InsertEmploymentActionField.CITY_NAME, dbOperation.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
                put(InsertEmploymentActionField.PERSON_EMAIL, dbOperation.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
                put(InsertEmploymentActionField.COMPANY_NUMBER, dbOperation.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
                put(InsertEmploymentActionField.START_DATE, dbOperation.getOnlyAttributeOfThing(answer, EMPLOYMENT, START_DATE));
                put(InsertEmploymentActionField.WAGE, dbOperation.getOnlyAttributeOfThing(answer, WAGE, WAGE_VALUE));
                put(InsertEmploymentActionField.CURRENCY, dbOperation.getOnlyAttributeOfThing(answer, WAGE, CURRENCY));
                put(InsertEmploymentActionField.CONTRACT_CONTENT, dbOperation.getOnlyAttributeOfThing(answer, CONTRACT, CONTRACT_CONTENT));
                put(InsertEmploymentActionField.CONTRACTED_HOURS, dbOperation.getOnlyAttributeOfThing(answer, CONTRACT, CONTRACTED_HOURS));
            }};
    }
}
