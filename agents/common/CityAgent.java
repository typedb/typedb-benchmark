package grakn.simulation.agents.common;

import grakn.simulation.world.World;
import grakn.simulation.agents.base.Agent;

public abstract class CityAgent extends Agent<World.City> {
    protected World.City city() {
        return item();
    }
}
