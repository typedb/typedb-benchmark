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

import grakn.benchmark.neo4j.driver.Neo4jOperation;
import grakn.benchmark.simulation.action.read.UnmarriedPeopleInCityAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.benchmark.neo4j.action.Model.GENDER;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;

public class Neo4jUnmarriedPeopleInCityAction extends UnmarriedPeopleInCityAction<Neo4jOperation> {
    public Neo4jUnmarriedPeopleInCityAction(Neo4jOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public List<String> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put(LOCATION_NAME, city.name());
            put(GENDER, gender);
            put("dobOfAdults", dobOfAdults);
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "email", null);
    }

    public static String query() {
        return "MATCH (person:Person {gender: $gender})-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "WHERE NOT (person)-[:RELOCATED_TO]->()\n" +
                "AND datetime(person.dateOfBirth) <= datetime($dobOfAdults)\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "RETURN person.email AS email\n" +
                "UNION\n" +
                "MATCH (person:Person)-[relocatedTo:RELOCATED_TO]->(city:City)\n" +
                "WITH person, city, relocatedTo.relocationDate AS relocDate\n" +
                "ORDER BY relocDate DESC\n" +
                "WITH person.email AS email, collect(relocDate)[0] AS lastRelocDate, collect(city)[0] as lastCity\n" +
                "WHERE lastCity.locationName = $locationName\n" +
                "RETURN email;";
    }
}
