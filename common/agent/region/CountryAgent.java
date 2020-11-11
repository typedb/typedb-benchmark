package grakn.simulation.common.agent.region;

import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.agent.base.Agent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class CountryAgent<DB_OPERATION extends DbOperation> extends Agent<World.Country, DB_OPERATION> {

    public CountryAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected List<World.Country> getRegions(World world) {
        return world.getCountries().collect(toList());
    }
}