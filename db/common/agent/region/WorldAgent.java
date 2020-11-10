package grakn.simulation.db.common.agent.region;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.world.World;

import java.util.Collections;
import java.util.List;

public abstract class WorldAgent<DB_OPERATION extends DbOperation> extends Agent<World, DB_OPERATION> {

    public WorldAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected List<World> getRegions(World world) {
        return Collections.singletonList(world);
    }
}
