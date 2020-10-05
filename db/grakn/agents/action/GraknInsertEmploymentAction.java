package grakn.simulation.db.grakn.agents.action;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.action.InsertEmploymentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.agents.interaction.GraknDbOperationController;
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

public class GraknInsertEmploymentAction extends InsertEmploymentAction<GraknDbOperationController.TransactionalDbOperation, ConceptMap> {
    public GraknInsertEmploymentAction(GraknDbOperationController.TransactionalDbOperation dbOperation, World.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
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
        return singleResult(dbOperation.tx().execute(insertEmploymentQuery));
    }

    @Override
    public HashMap<String, Object> outputForReport(ConceptMap answer) {
        return new HashMap<String, Object>() {{
                put("CITY_NAME", dbOperation.tx().getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
                put("PERSON_EMAIL", dbOperation.tx().getOnlyAttributeOfThing(answer, PERSON, EMAIL));
                put("COMPANY_NUMBER", dbOperation.tx().getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
                put("START_DATE", dbOperation.tx().getOnlyAttributeOfThing(answer, EMPLOYMENT, START_DATE));
                put("WAGE", dbOperation.tx().getOnlyAttributeOfThing(answer, WAGE, WAGE_VALUE));
                put("CURRENCY", dbOperation.tx().getOnlyAttributeOfThing(answer, WAGE, CURRENCY));
                put("CONTRACT_CONTENT", dbOperation.tx().getOnlyAttributeOfThing(answer, CONTRACT, CONTRACT_CONTENT));
                put("CONTRACTED_HOURS", dbOperation.tx().getOnlyAttributeOfThing(answer, CONTRACT, CONTRACTED_HOURS));
            }};
    }
}
