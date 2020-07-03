package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;

public abstract class CityAgent extends Agent<World.City> {
    protected World.City city() {
        return item();
    }
    public Class runnerType() {
        return CityAgentRunner.class;
    }
}
