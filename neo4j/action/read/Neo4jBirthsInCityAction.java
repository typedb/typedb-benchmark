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
import grakn.benchmark.simulation.action.read.BirthsInCityAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jBirthsInCityAction extends BirthsInCityAction<Neo4jTransaction> {

    public Neo4jBirthsInCityAction(Neo4jTransaction tx, World.City city, LocalDateTime today) {
        super(tx, city, today);
    }

    @Override
    public List<String> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("dateOfBirth", today);
            put("locationName", worldCity.name());
        }};
        return tx.sortedExecute(new Query(query(), parameters), "child.email", null);
    }

    public static String query() {
        return "MATCH (city:City {locationName: $locationName}),\n" +
                "(child:Person {dateOfBirth: $dateOfBirth})-[:BORN_IN]->(city)\n" +
                "RETURN child.email";
    }
}
