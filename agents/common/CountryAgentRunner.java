package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentContext;
import grakn.simulation.agents.base.AgentRunner;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CountryAgentRunner extends AgentRunner<World.Country> {

    protected CountryAgentRunner(Class<? extends Agent<World.Country>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<World.Country> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getCountries().collect(toList());
    }
}