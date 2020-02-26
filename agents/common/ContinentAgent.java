package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;

public abstract class ContinentAgent extends Agent<World.Continent> {

    protected World.Continent continent() {
        return item();
    }
}
