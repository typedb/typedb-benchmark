/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.grakn.agent;

import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.simulation.agent.EmploymentAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.benchmark.grakn.agent.Types.CITY;
import static grakn.benchmark.grakn.agent.Types.COMPANY;
import static grakn.benchmark.grakn.agent.Types.COMPANY_NUMBER;
import static grakn.benchmark.grakn.agent.Types.CONTRACT;
import static grakn.benchmark.grakn.agent.Types.CONTRACTED_HOURS;
import static grakn.benchmark.grakn.agent.Types.CONTRACT_CONTENT;
import static grakn.benchmark.grakn.agent.Types.COUNTRY;
import static grakn.benchmark.grakn.agent.Types.CURRENCY;
import static grakn.benchmark.grakn.agent.Types.EMAIL;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_CONTRACT;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_EMPLOYEE;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_EMPLOYER;
import static grakn.benchmark.grakn.agent.Types.EMPLOYMENT_WAGE;
import static grakn.benchmark.grakn.agent.Types.LOCATES;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATED;
import static grakn.benchmark.grakn.agent.Types.LOCATES_LOCATION;
import static grakn.benchmark.grakn.agent.Types.LOCATION_HIERARCHY;
import static grakn.benchmark.grakn.agent.Types.LOCATION_NAME;
import static grakn.benchmark.grakn.agent.Types.PERSON;
import static grakn.benchmark.grakn.agent.Types.START_DATE;
import static grakn.benchmark.grakn.agent.Types.WAGE;
import static grakn.benchmark.grakn.agent.Types.WAGE_VALUE;
import static graql.lang.Graql.match;
import static graql.lang.Graql.var;

public class GraknEmploymentAgent extends EmploymentAgent<GraknTransaction> {

    public GraknEmploymentAgent(Client<?, GraknTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(GraknTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return GraknMatcher.matchResidentsInCity(tx, city, numResidents, earliestDate);
    }

    @Override
    protected List<Long> matchCompaniesInCountry(GraknTransaction tx, GeoData.Country country, int numCompanies) {
        return GraknMatcher.matchCompaniesInCountry(tx, country, numCompanies);
    }

    @Override
    protected void insertEmployment(GraknTransaction tx, GeoData.City city, String employeeEmail, Long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        tx.execute(match(
                var(CITY).isa(CITY).has(LOCATION_NAME, city.name()),
                var(PERSON).isa(PERSON).has(EMAIL, employeeEmail),
                var(COMPANY).isa(COMPANY).has(COMPANY_NUMBER, companyNumber),
                var(COUNTRY).isa(COUNTRY).has(CURRENCY, var(CURRENCY)),
                var(LOCATION_HIERARCHY).rel(var(CITY)).rel(var(COUNTRY)).isa(LOCATION_HIERARCHY)
        ).insert(
                var(EMPLOYMENT).rel(EMPLOYMENT_EMPLOYEE, var(PERSON)).rel(EMPLOYMENT_EMPLOYER, var(COMPANY))
                        .rel(EMPLOYMENT_CONTRACT, var(CONTRACT)).rel(EMPLOYMENT_WAGE, var(WAGE))
                        .isa(EMPLOYMENT).has(START_DATE, employmentDate),
                var(WAGE).isa(WAGE).has(WAGE_VALUE, wageValue).has(CURRENCY, var(CURRENCY)), //TODO Should this be inferred rather than inserted?
                var(LOCATES).rel(LOCATES_LOCATION, var(CITY)).rel(LOCATES_LOCATED, var(EMPLOYMENT)).isa(LOCATES),
                var(CONTRACT).isa(CONTRACT).has(CONTRACT_CONTENT, contractContent).has(CONTRACTED_HOURS, contractedHours)
        ));
    }

    //    @Override
//    public HashMap<ComparableField, Object> outputForReport(ConceptMap answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertEmploymentActionField.CITY_NAME, tx.getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
//            put(InsertEmploymentActionField.PERSON_EMAIL, tx.getOnlyAttributeOfThing(answer, PERSON, EMAIL));
//            put(InsertEmploymentActionField.COMPANY_NUMBER, tx.getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
//            put(InsertEmploymentActionField.START_DATE, tx.getOnlyAttributeOfThing(answer, EMPLOYMENT, START_DATE));
//            put(InsertEmploymentActionField.WAGE, tx.getOnlyAttributeOfThing(answer, WAGE, WAGE_VALUE));
//            put(InsertEmploymentActionField.CURRENCY, tx.getOnlyAttributeOfThing(answer, WAGE, CURRENCY));
//            put(InsertEmploymentActionField.CONTRACT_CONTENT, tx.getOnlyAttributeOfThing(answer, CONTRACT, CONTRACT_CONTENT));
//            put(InsertEmploymentActionField.CONTRACTED_HOURS, tx.getOnlyAttributeOfThing(answer, CONTRACT, CONTRACTED_HOURS));
//        }};
//    }
}
