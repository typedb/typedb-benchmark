package grakn.simulation.db.common.agent.region;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class WorldAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends Agent<World, DB_DRIVER, DB_OPERATION> {

    public WorldAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected List<World> getRegions(World world) {
        return Collections.singletonList(world);
    }
}
