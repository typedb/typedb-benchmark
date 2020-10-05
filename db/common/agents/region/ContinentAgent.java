package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class ContinentAgent<DB_DRIVER extends DbDriver> extends Agent<World.Continent, DB_DRIVER> {

    public ContinentAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected List<World.Continent> getRegions(World world) {
        return world.getContinents().collect(toList());
    }
}
