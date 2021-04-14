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
import grakn.benchmark.simulation.agent.CompanyAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jCompanyAgent extends CompanyAgent<Neo4jTransaction> {

    public Neo4jCompanyAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void insertCompany(Neo4jTransaction tx, GeoData.Country country, LocalDateTime today, int companyNumber, String companyName) {
        String query = "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (company:Company {companyNumber: $companyNumber, companyName: $companyName})-[incorporation:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(country)" +
                "RETURN company.companyName, company.companyNumber, country.locationName, incorporation.dateOfIncorporation";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("countryName", country.name());
            put("companyNumber", companyNumber);
            put("companyName", companyName);
            put("dateOfIncorporation", today);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<>() {
//            {
//                put(InsertCompanyActionField.COMPANY_NAME, answer.asMap().get("company." + COMPANY_NAME));
//                put(InsertCompanyActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
//                put(InsertCompanyActionField.COUNTRY, answer.asMap().get("country." + LOCATION_NAME));
//                put(InsertCompanyActionField.DATE_OF_INCORPORATION, answer.asMap().get("incorporation." + DATE_OF_INCORPORATION));
//            }
//        };
//    }
}
