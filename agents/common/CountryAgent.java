package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentRunner;

public abstract class CountryAgent extends Agent<World.Country> {

    protected World.Country country() {
        return item();
    }
    @Override
    public Class<? extends AgentRunner<?>> runnerType() {
        return CountryAgentRunner.class;
    }
}
