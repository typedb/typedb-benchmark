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
import grakn.benchmark.simulation.agent.AgeUpdateAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jAgeUpdateAgent extends AgeUpdateAgent<Neo4jTransaction> {

    public Neo4jAgeUpdateAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected void updateAgesOfPeopleInCity(Neo4jTransaction tx, LocalDateTime today, GeoData.City city) {
        String query = "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n" +
                "RETURN person.age";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put(Labels.LOCATION_NAME, city.name());
            put("dateToday", today);
        }};
        tx.execute(new Query(query, parameters));
    }
}
