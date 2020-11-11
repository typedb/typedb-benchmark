package grakn.simulation.db.neo4j.action.read;

import grakn.simulation.db.common.action.read.ResidentsInCityAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperation;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Neo4jResidentsInCityAction extends ResidentsInCityAction<Neo4jOperation> {

    public Neo4jResidentsInCityAction(Neo4jOperation dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public List<String> run() {
        String template = query();
        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put("locationName", city.name());
            put("earliestDate", earliestDate);
        }};
        return dbOperation.sortedExecute(new Query(template, parameters), "resident.email", numResidents);
    }

    public static String query() {
        return "MATCH (resident:Person)-[residentOf:RESIDENT_OF]->(city:City {locationName: $locationName})" +
                "WHERE datetime(residentOf.startDate) <= datetime($earliestDate) AND NOT EXISTS (residentOf.endDate)\n" +
                "RETURN resident.email";
    }
}
