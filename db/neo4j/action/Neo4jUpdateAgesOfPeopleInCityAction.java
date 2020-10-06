package grakn.simulation.db.neo4j.action;

import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jDbOperationController;
import grakn.simulation.db.neo4j.driver.Neo4jTransaction;
import grakn.simulation.db.neo4j.schema.Schema;
import org.neo4j.driver.Query;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Neo4jUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<Neo4jDbOperationController.TransactionalDbOperation> {
    public Neo4jUpdateAgesOfPeopleInCityAction(TransactionDbOperationController<Neo4jTransaction>.TransactionalDbOperation dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation, today, city);
    }

    @Override
    public Integer run() {
        String template = "" +
                "MATCH (person:Person)-[:BORN_IN]->(city:City {locationName: $locationName})\n" +
                "SET person.age = duration.between(person.dateOfBirth, localdatetime($dateToday)).years\n" +
                "RETURN person.age";

        HashMap<String, Object> parameters = new HashMap<String, Object>(){{
            put(Schema.LOCATION_NAME, city.name());
            put("dateToday", today);
        }};

        Query query = new Query(template, parameters);
        dbOperation.tx().execute(query);
        return null;
    }

}
