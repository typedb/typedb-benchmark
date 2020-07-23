package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.world.World;

public abstract class CityAgent<S extends DriverWrapper.Session<S, T>, T extends DriverWrapper.Transaction> extends Agent<World.City, S, T> {

    protected World.City city() {
        return item();
    }
}
