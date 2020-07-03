package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentRunner;

public abstract class ContinentAgent extends Agent<World.Continent> {

    protected World.Continent continent() {
        return item();
    }
    @Override
    public Class<? extends AgentRunner<?>> runnerType() {
        return ContinentAgentRunner.class;
    }
}
