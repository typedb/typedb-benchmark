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
import grakn.benchmark.simulation.action.write.InsertMarriageAction;
import grakn.benchmark.simulation.world.World;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.util.HashMap;

import static grakn.benchmark.neo4j.action.Model.EMAIL;
import static grakn.benchmark.neo4j.action.Model.LOCATION_NAME;
import static grakn.benchmark.neo4j.action.Model.MARRIAGE_ID;

public class Neo4jInsertMarriageAction extends InsertMarriageAction<Neo4jTransaction, Record> {

    public Neo4jInsertMarriageAction(Neo4jTransaction dbOperation, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        super(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail);
    }

    @Override
    public Record run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>() {{
            put(MARRIAGE_ID, marriageIdentifier);
            put("wifeEmail", wifeEmail);
            put("husbandEmail", husbandEmail);
            put(LOCATION_NAME, worldCity.name());
        }};
        return Action.singleResult(dbOperation.execute(new Query(query(), parameters)));
    }

    public static String query() {
        return "MATCH (wife:Person {email: $wifeEmail}), (husband:Person {email: $husbandEmail}), (city:City {locationName: $locationName})\n" +
                "CREATE (husband)-[marriage:MARRIED_TO {marriageId: $marriageId, locationName: city.locationName}]->(wife)" +
                "RETURN marriage.marriageId, husband.email, wife.email, city.locationName";
    }

    @Override
    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
        return new HashMap<ComparableField, Object>() {{
            put(InsertMarriageActionField.MARRIAGE_IDENTIFIER, answer.asMap().get("marriage." + MARRIAGE_ID));
            put(InsertMarriageActionField.WIFE_EMAIL, answer.asMap().get("wife." + EMAIL));
            put(InsertMarriageActionField.HUSBAND_EMAIL, answer.asMap().get("husband." + EMAIL));
            put(InsertMarriageActionField.CITY_NAME, answer.asMap().get("city." + LOCATION_NAME));
        }};
    }
}
