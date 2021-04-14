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

package grakn.benchmark.neo4j.agent;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.agent.EmploymentAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jEmploymentAgent extends EmploymentAgent<Neo4jTransaction> {

    public Neo4jEmploymentAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(Neo4jTransaction tx, GeoData.City region, int numResidents, LocalDateTime earliestDate) {
        return Neo4jMatcher.matchResidentsInCity(tx, region, numResidents, earliestDate);
    }

    @Override
    protected List<Long> matchCompaniesInCountry(Neo4jTransaction tx, GeoData.Country country, int numCompanies) {
        return Neo4jMatcher.matchCompaniesInCountry(tx, country, numCompanies);
    }

    @Override
    protected void insertEmployment(Neo4jTransaction tx, GeoData.City city, String employeeEmail, Long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        String query = "MATCH (city:City {locationName: $locationName})-[:LOCATED_IN]->(country:Country),\n" +
                "(person:Person {email: $email}),\n" +
                "(company:Company {companyNumber: $companyNumber})\n" +
                "CREATE (company)-[employs:EMPLOYS {\n" +
                "   startDate: $startDate,\n" +
                "   wage: $wage,\n" +
                "   currency: country.currency,\n" +
                "   locationName: city.locationName,\n" +
                "   contractContent: $contractContent,\n" +
                "   contractedHours: $contractedHours}\n" +
                "]->(person)\n" +
                "RETURN city.locationName, person.email, company.companyNumber, country.locationName, \n" +
                "employs.startDate, employs.wage, employs.currency, employs.contractContent, employs.contractedHours";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("locationName", city.name());
            put("email", employeeEmail);
            put("companyNumber", companyNumber);
            put("startDate", employmentDate);
            put("wage", wageValue);
            put("contractContent", contractContent);
            put("contractedHours", contractedHours);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    public HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertEmploymentActionField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
//            put(InsertEmploymentActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
//            put(InsertEmploymentActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
//            put(InsertEmploymentActionField.START_DATE, answer.asMap().get("employs." + START_DATE));
//            put(InsertEmploymentActionField.WAGE, answer.asMap().get("employs." + WAGE));
//            put(InsertEmploymentActionField.CURRENCY, answer.asMap().get("employs." + CURRENCY));
//            put(InsertEmploymentActionField.CONTRACT_CONTENT, answer.asMap().get("employs." + CONTRACT_CONTENT));
//            put(InsertEmploymentActionField.CONTRACTED_HOURS, answer.asMap().get("employs." + CONTRACTED_HOURS));
//        }};
//    }
}
