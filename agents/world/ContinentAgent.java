package grakn.simulation.agents.world;

import grakn.simulation.agents.base.Agent;
import grakn.simulation.world.World;

public abstract class ContinentAgent extends Agent<World.Continent> {

    protected World.Continent continent() {
        return item();
    }
}
