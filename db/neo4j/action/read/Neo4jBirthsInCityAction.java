package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jBirthsInCityAction extends BirthsInCityAction<Neo4jOperation> {
    public Neo4jBirthsInCityAction(Neo4jOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<String> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("dateOfBirth", today);
            put("locationName", worldCity.name());
        }};
        return dbOperation.getOrderedAttribute(new Query(query(), parameters), "child.email", null);
    }

    public static String query() {
        return "MATCH (city:City {locationName: $locationName}),\n" +
                "(child:Person {dateOfBirth: $dateOfBirth})-[:BORN_IN]->(city)\n" +
                "RETURN child.email";
    }
}
