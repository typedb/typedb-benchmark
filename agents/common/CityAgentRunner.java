package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentContext;
import grakn.simulation.agents.base.AgentRunner;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CityAgentRunner extends AgentRunner<World.City> {

    public CityAgentRunner(Class<? extends Agent<World.City>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<World.City> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getCities().collect(toList());
    }
}

