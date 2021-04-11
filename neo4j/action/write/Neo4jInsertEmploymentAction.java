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

package grakn.benchmark.neo4j.action.write;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.write.InsertEmploymentAction;
import grakn.benchmark.simulation.common.GeoData;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.COMPANY_NUMBER;
import static grakn.benchmark.neo4j.action.Model.CONTRACTED_HOURS;
import static grakn.benchmark.neo4j.action.Model.CONTRACT_CONTENT;
import static grakn.benchmark.neo4j.action.Model.CURRENCY;
import static grakn.benchmark.neo4j.action.Model.EMAIL;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;
import static grakn.benchmark.neo4j.action.Model.START_DATE;
import static grakn.benchmark.neo4j.action.Model.WAGE;

public class Neo4jInsertEmploymentAction extends InsertEmploymentAction<Neo4jTransaction, Record> {

    public Neo4jInsertEmploymentAction(Neo4jTransaction tx, GeoData.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
        super(tx, worldCity, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("locationName", worldCity.name());
            put("email", employeeEmail);
            put("companyNumber", companyNumber);
            put("startDate", employmentDate);
            put("wage", wageValue);
            put("contractContent", contractContent);
            put("contractedHours", contractedHours);
        }};
        return singleResult(tx.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (city:City {locationName: $locationName})-[:LOCATED_IN]->(country:Country),\n" +
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
    }

    @Override
    public HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertEmploymentActionField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
            put(InsertEmploymentActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
            put(InsertEmploymentActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
            put(InsertEmploymentActionField.START_DATE, answer.asMap().get("employs." + START_DATE));
            put(InsertEmploymentActionField.WAGE, answer.asMap().get("employs." + WAGE));
            put(InsertEmploymentActionField.CURRENCY, answer.asMap().get("employs." + CURRENCY));
            put(InsertEmploymentActionField.CONTRACT_CONTENT, answer.asMap().get("employs." + CONTRACT_CONTENT));
            put(InsertEmploymentActionField.CONTRACTED_HOURS, answer.asMap().get("employs." + CONTRACTED_HOURS));
        }};
    }
}
