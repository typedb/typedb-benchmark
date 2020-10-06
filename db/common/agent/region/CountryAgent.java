package grakn.simulation.db.common.agent.region;

import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class CountryAgent<DB_DRIVER extends DbDriver> extends Agent<World.Country, DB_DRIVER> {

    public CountryAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected List<World.Country> getRegions(World world) {
        return world.getCountries().collect(toList());
    }
}