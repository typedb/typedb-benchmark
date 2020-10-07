package grakn.simulation.db.neo4j.action;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.driver.Neo4jOperationFactory;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;

public class Neo4jActionFactory extends ActionFactory<?, Record> {
    public Neo4jActionFactory(Neo4jOperationFactory dbOperationFactory) {
        super(dbOperationFactory);
    }

    @Override
    public UpdateAgesOfPeopleInCityAction<?> updateAgesOfPeopleInCityAction(LocalDateTime today, World.City city) {
        return new Neo4jUpdateAgesOfPeopleInCityAction(dbOperationFactory.dbOperation(), today, city);
    }
}
