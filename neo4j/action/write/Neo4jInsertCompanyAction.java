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

package grakn.benchmark.neo4j.action.write;

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.write.InsertCompanyAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.COMPANY_NAME;
import static grakn.benchmark.neo4j.action.Model.COMPANY_NUMBER;
import static grakn.benchmark.neo4j.action.Model.DATE_OF_INCORPORATION;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;

public class Neo4jInsertCompanyAction extends InsertCompanyAction<Neo4jTransaction, Record> {

    public Neo4jInsertCompanyAction(Neo4jTransaction dbOperation, World.Country country, LocalDateTime today, int companyNumber, String companyName) {
        super(dbOperation, country, today, companyNumber, companyName);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("countryName", country.name());
            put("companyNumber", companyNumber);
            put("companyName", companyName);
            put("dateOfIncorporation", today);
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (country:Country {locationName: $countryName})\n" +
                "CREATE (company:Company {companyNumber: $companyNumber, companyName: $companyName})-[incorporation:INCORPORATED_IN {dateOfIncorporation: $dateOfIncorporation}]->(country)" +
                "RETURN company.companyName, company.companyNumber, country.locationName, incorporation.dateOfIncorporation";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {
            {
                put(InsertCompanyActionField.COMPANY_NAME, answer.asMap().get("company." + COMPANY_NAME));
                put(InsertCompanyActionField.COMPANY_NUMBER, answer.asMap().get("company." + COMPANY_NUMBER));
                put(InsertCompanyActionField.COUNTRY, answer.asMap().get("country." + LOCATION_NAME));
                put(InsertCompanyActionField.DATE_OF_INCORPORATION, answer.asMap().get("incorporation." + DATE_OF_INCORPORATION));
            }
        };
    }
}
