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

import grakn.benchmark.neo4j.action.Model;
import grakn.benchmark.neo4j.driver.Neo4jTransaction;
import grakn.benchmark.simulation.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<Neo4jTransaction> {
    public Neo4jUpdateAgesOfPeopleInCityAction(Neo4jTransaction dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation, today, city);
    }

    @Override
    public Integer run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put(Model.LOCATION_NAME, city.name());
            put("dateToday", today);
        }};
        dbOperation.execute(new Query(query(), parameters));
        return null;
    }

    public static String query() {
        return "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n" +
                "RETURN person.age";
    }
}
