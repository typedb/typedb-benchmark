package grakn.simulation.agents.world;

import grakn.simulation.agents.base.Agent;
import grakn.simulation.world.World;

public abstract class CityAgent extends Agent<World.City> {
    protected World.City city() {
        return item();
    }
}
