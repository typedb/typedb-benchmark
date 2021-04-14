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
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.agent.ParentshipAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Neo4jParentshipAgent extends ParentshipAgent<Neo4jTransaction> {

    public Neo4jParentshipAgent(Client<?, Neo4jTransaction> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<String> matchBirthsInCity(Neo4jTransaction tx, GeoData.City city, LocalDateTime today) {
        return Neo4jMatcher.matchBirthsInCity(tx, city, today);
    }

    @Override
    public List<HashMap<MarriageAgent.SpouseType, String>> matchMarriedCouple(Neo4jTransaction tx, GeoData.City city) {
        String query = "MATCH (city:City {locationName: $locationName}),\n" +
                "(husband:Person)-[marriage:MARRIED_TO {locationName: city.locationName}]->(wife:Person)\n" +
                "WHERE NOT (wife)<-[:CHILD_OF]-(:Person)-[:CHILD_OF]->(husband)\n" +
                "RETURN husband.email, wife.email\n" +
                "ORDER BY marriage.id ASC\n";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("locationName", city.name());
        }};
        List<Record> records = tx.execute(new Query(query, parameters));
        return records.stream().map(Record::asMap).map(r -> new HashMap<MarriageAgent.SpouseType, String>() {{
            put(MarriageAgent.SpouseType.WIFE, r.get("wife.email").toString());
            put(MarriageAgent.SpouseType.HUSBAND, r.get("husband.email").toString());
        }}).collect(toList());
    }

    @Override
    protected void insertParentship(Neo4jTransaction tx, HashMap<MarriageAgent.SpouseType, String> marriage, String childEmail) {
        String query = "MATCH (mother:Person {email: $motherEmail}), (father:Person {email: $fatherEmail}),\n" +
                "(child:Person {email: $childEmail})\n" +
                "CREATE (father)<-[:CHILD_OF]-(child)-[:CHILD_OF]->(mother)\n" +
                "RETURN mother.email, father.email, child.email";
        HashMap<String, Object> parameters = new HashMap<>() {{
            put("motherEmail", marriage.get(MarriageAgent.SpouseType.WIFE));
            put("fatherEmail", marriage.get(MarriageAgent.SpouseType.HUSBAND));
            put("childEmail", childEmail);
        }};
        tx.execute(new Query(query, parameters));
    }

//    @Override
//    protected HashMap<ComparableField, Object> outputForReport(Record answer) {
//        return new HashMap<ComparableField, Object>() {
//            {
//                put(InsertParentShipActionField.WIFE_EMAIL, answer.asMap().get("mother." + EMAIL));
//                put(InsertParentShipActionField.HUSBAND_EMAIL, answer.asMap().get("father." + EMAIL));
//                put(InsertParentShipActionField.CHILD_EMAIL, answer.asMap().get("child." + EMAIL));
//            }
//        };
//    }
//
//    public enum InsertParentShipActionField implements ComparableField {
//        WIFE_EMAIL, HUSBAND_EMAIL, CHILD_EMAIL
//    }
}
