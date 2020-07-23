package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.world.World;

public abstract class CountryAgent <S extends DriverWrapper.Session<S, T>, T extends DriverWrapper.Transaction> extends Agent<World.Country, S, T> {

    protected World.Country country() {
        return item();
    }
}
