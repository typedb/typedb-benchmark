package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.neo4j.schema.Schema.EMAIL;
import static grakn.simulation.db.neo4j.schema.Schema.GENDER;
import static grakn.simulation.db.neo4j.schema.Schema.LOCATION_NAME;

public class Neo4jUnmarriedPeopleInCityAction extends UnmarriedPeopleInCityAction<Neo4jOperation> {
    public Neo4jUnmarriedPeopleInCityAction(Neo4jOperation dbOperation, World.City city, String gender, LocalDateTime dobOfAdults) {
        super(dbOperation, city, gender, dobOfAdults);
    }

    @Override
    public List<String> run() {
        String template = "" +
                "MATCH (person:Person {gender: $gender})-[residentOf:RESIDENT_OF]->(city:City {locationName: $locationName})\n" +
                "WHERE datetime(person.dateOfBirth) <= datetime(\"" + dobOfAdults + "\")\n" +
                "AND NOT (person)-[:MARRIED_TO]-()\n" +
                "AND NOT EXISTS (residentOf.endDate)\n" +
                "RETURN person.email";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(LOCATION_NAME, city.name());
            put(GENDER, gender);
        }};
        return dbOperation.getOrderedAttribute(new Query(template, parameters), "person." + EMAIL, null);
    }
}
