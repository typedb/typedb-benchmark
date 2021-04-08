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
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.write.InsertRelocationAction;
import grakn.benchmark.simulation.common.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.EMAIL;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;
import static grakn.benchmark.neo4j.action.Model.RELOCATION_DATE;

public class Neo4jInsertRelocationAction extends InsertRelocationAction<Neo4jTransaction, Record> {

    public Neo4jInsertRelocationAction(Neo4jTransaction tx, World.City city, LocalDateTime today, String relocateeEmail, String relocationCityName) {
        super(tx, city, today, relocateeEmail, relocationCityName);
    }

    @Override
    public Record run() {
        // This raises questions over whether the person's ResidentOf end-date should be updated in this step, or
        // figured out at query-time, which would be more in-line with Grakn

        // In either case, their old ResidentOf should be given an `endDate`, and they should have a new ResidentOf
        // alongside this relation

        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put("email", relocateeEmail);
            put("newCityName", relocationCityName);
            put("relocationDate", today);
        }};
        return Action.singleResult(tx.execute(new Query(createRelocationQuery(), parameters)));
    }

    public static String createRelocationQuery() {
        // Not making this ternary is losing the information of where the person if relocating from
        return "MATCH (person:Person {email: $email}), (newCity:City {locationName: $newCityName})\n" +
                "CREATE (person)-[relocatedTo:RELOCATED_TO {relocationDate:$relocationDate}]->(newCity)" +
                "RETURN person.email, newCity.locationName, relocatedTo.relocationDate";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertRelocationActionField.PERSON_EMAIL, answer.asMap().get("person." + EMAIL));
            put(InsertRelocationActionField.NEW_CITY_NAME, answer.asMap().get("newCity." + LOCATION_NAME));
            put(InsertRelocationActionField.RELOCATION_DATE, answer.asMap().get("relocatedTo." + RELOCATION_DATE));
        }};
    }
}
