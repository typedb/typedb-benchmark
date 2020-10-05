package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class CityAgent<DB_DRIVER extends DbDriver> extends Agent<World.City, DB_DRIVER> {

    public CityAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected List<World.City> getRegions(World world) {
        return world.getCities().collect(toList());
    }
}
