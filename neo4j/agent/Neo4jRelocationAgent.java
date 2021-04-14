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
import grakn.benchmark.simulation.agent.RelocationAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jRelocationAgent extends RelocationAgent<Neo4jTransaction> {

    public Neo4jRelocationAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchResidentsInCity(Neo4jTransaction tx, GeoData.City city, int numResidents, LocalDateTime earliestDate) {
        return Neo4jMatcher.matchResidentsInCity(tx, city, numResidents, earliestDate);
    }

    @Override
    protected List<String> matchCitiesInContinent(Neo4jTransaction tx, GeoData.City city) {
        return Neo4jMatcher.matchCitiesInContinent(tx, city);
    }

    @Override
    protected void insertRelocation(Neo4jTransaction tx, GeoData.City city, LocalDateTime today, String residentEmail, String relocationCityName) {
        // This raises questions over whether the person's ResidentOf end-date should be updated in this step, or
        // figured out at query-time, which would be more in-line with Grakn

        // In either case, their old ResidentOf should be given an `endDate`, and they should have a new ResidentOf
        // alongside this relation

        // Not making this ternary is losing the information of where the person if relocating from
        String query = "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[relocatedTo:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity)" +
                "RETURN person.email, newCity.locationName, relocatedTo.relocationDate";

        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("email", residentEmail);
            put("newCityName", relocationCityName);
            put("relocationDate", today);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<ComparableField, Object>() {{
//            put(InsertRelocationActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
//            put(InsertRelocationActionField.NEW_CITY_NAME, answer.asMap().get("newCity." + LOCATION_NAME));
//            put(InsertRelocationActionField.RELOCATION_DATE, answer.asMap().get("relocatedTo." + RELOCATION_DATE));
//        }};
//    }
}
