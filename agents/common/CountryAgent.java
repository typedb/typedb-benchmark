package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;

public abstract class CountryAgent extends Agent<World.Country> {

    protected World.Country country() {
        return item();
    }
}
