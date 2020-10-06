package grakn.simulation.db.neo4j.action;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jDbOperationController;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;

public class Neo4jActionFactory extends ActionFactory<Neo4jDbOperationController, Record> {
    public Neo4jActionFactory(Neo4jDbOperationController dbOperationController) {
        super(dbOperationController);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<?> updateAgesOfPeopleInCityAction(LocalDateTime today, World.City city) {
        return new Neo4jUpdateAgesOfPeopleInCityAction(dbOpController.dbOperation(), today, city);
    }
}
