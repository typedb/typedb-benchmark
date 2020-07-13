package grakn.simulation.agents.world;

import grakn.simulation.agents.base.Agent;
import grakn.simulation.world.World;

public abstract class CountryAgent extends Agent<World.Country> {

    protected World.Country country() {
        return item();
    }
}
