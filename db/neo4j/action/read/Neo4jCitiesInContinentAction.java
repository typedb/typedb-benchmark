package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.util.HashMap;
import java.util.List;

public class Neo4jCitiesInContinentAction extends CitiesInContinentAction<Neo4jOperation> {
    public Neo4jCitiesInContinentAction(Neo4jOperation dbOperation, World.City city) {
        super(dbOperation, city);
    }

    @Override
    public List<String> run() {
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("continentName", city.country().continent().name());
            put("cityName", city.name());
        }};
        return dbOperation.sortedExecute(new Query(query(), parameters), "city.locationName", null);
    }

    public static String query() {
        return "MATCH (city:City)-[:LOCATED_IN*2]->(continent:Continent {locationName: $continentName})\n" +
                "WHERE NOT city.locationName = $cityName\n" +
                "RETURN city.locationName";
    }
}
