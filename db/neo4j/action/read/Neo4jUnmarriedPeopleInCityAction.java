package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.schema.Schema.GENDER;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;

public class Neo4jUnmarriedPeopleInCityAction extends UnmarriedPeopleInCityAction<Neo4jOperation> {
    public Neo4jUnmarriedPeopleInCityAction(Neo4jOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public List<String> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
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
