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

import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.read.CompaniesInContinentAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jCompaniesInContinentAction extends CompaniesInContinentAction<Neo4jTransaction> {
    public Neo4jCompaniesInContinentAction(Neo4jTransaction dbOperation, World.Continent continent) {
        super(dbOperation, continent);
    }

    @Override
    public List<Long> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("continentName", continent.name());
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "company.companyNumber", null);
    }

    public static String query() {
        return "MATCH (company:Company)-[:INCORPORATED_IN]->(country:Country)-[:LOCATED_IN]->(continent:Continent {locationName: $continentName})\n" +
                "RETURN company.companyNumber";
    }
}
