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

package grakn.benchmark.neo4j.action.read;

import grakn.benchmark.common.action.read.CompaniesInCountryAction;
import grakn.benchmark.common.world.World;
import grakn.benchmark.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jCompaniesInCountryAction extends CompaniesInCountryAction<Neo4jOperation> {
    public Neo4jCompaniesInCountryAction(Neo4jOperation dbOperation, World.Country country, int numCompanies) {
        super(dbOperation, country, numCompanies);
    }

    @Override
    public List<Long> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("countryName", country.name());
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "company.companyNumber", numCompanies);
    }

    public static String query() {
        return "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country {locationName: $countryName})\n" +
                "RETURN company.companyNumber";
    }
}
