/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.simulation.grakn.action.write;

import grakn.client.concept.answer.ConceptMap;
import grakn.simulation.common.action.write.InsertEmploymentAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.pattern.variable.UnboundVariable;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.COMPANY;
import static grakn.simulation.grakn.action.Model.COMPANY_NUMBER;
import static grakn.simulation.grakn.action.Model.CONTRACT;
import static grakn.simulation.grakn.action.Model.CONTRACTED_HOURS;
import static grakn.simulation.grakn.action.Model.CONTRACT_CONTENT;
import static grakn.simulation.grakn.action.Model.COUNTRY;
import static grakn.simulation.grakn.action.Model.CURRENCY;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_CONTRACT;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_EMPLOYEE;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_EMPLOYER;
import static grakn.simulation.grakn.action.Model.EMPLOYMENT_WAGE;
import static grakn.simulation.grakn.action.Model.LOCATES;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATED;
import static grakn.simulation.grakn.action.Model.LOCATES_LOCATION;
import static grakn.simulation.grakn.action.Model.LOCATION_HIERARCHY;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;
import static grakn.simulation.grakn.action.Model.START_DATE;
import static grakn.simulation.grakn.action.Model.WAGE;
import static grakn.simulation.grakn.action.Model.WAGE_VALUE;

public class GraknInsertEmploymentAction extends InsertEmploymentAction<GraknOperation, ConceptMap> {
    public GraknInsertEmploymentAction(GraknOperation dbOperation, World.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(dbOperation, worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public ConceptMap run() {
        return singleResult(dbOperation.execute(query(worldCity.name(), employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours)));
    }

    public static GraqlInsert query(String worldCityName, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        UnboundVariable city = Graql.var(CITY);
        UnboundVariable person = Graql.var(PERSON);
        UnboundVariable company = Graql.var(COMPANY);
        UnboundVariable country = Graql.var(COUNTRY);
        UnboundVariable locationHierarchy = Graql.var(LOCATION_HIERARCHY);
        UnboundVariable employment = Graql.var(EMPLOYMENT);
        UnboundVariable wage = Graql.var(WAGE);
        UnboundVariable locates = Graql.var(LOCATES);
        UnboundVariable contract = Graql.var(CONTRACT);
        UnboundVariable currency = Graql.var(CURRENCY);

        return Graql.match(
                city
                        .isa(CITY)
                        .has(LOCATION_NAME, worldCityName),
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
                        .rel(city)
                        .rel(country)
                        .isa(LOCATION_HIERARCHY)
        ).insert(
                employment
                        .rel(EMPLOYMENT_EMPLOYEE, person)
                        .rel(EMPLOYMENT_EMPLOYER, company)
                        .rel(EMPLOYMENT_CONTRACT, contract)
                        .rel(EMPLOYMENT_WAGE, wage)
                        .isa(EMPLOYMENT)
                        .has(START_DATE, employmentDate),
                wage
                        .isa(WAGE)
                        .has(WAGE_VALUE, wageValue)
                        .has(CURRENCY, currency), //TODO Should this be inferred rather than inserted?
                locates
                        .rel(LOCATES_LOCATION, city)
                        .rel(LOCATES_LOCATED, employment)
                        .isa(LOCATES),
                contract
                        .isa(CONTRACT)
                        .has(CONTRACT_CONTENT, contractContent)
                        .has(CONTRACTED_HOURS, contractedHours)
        );
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
