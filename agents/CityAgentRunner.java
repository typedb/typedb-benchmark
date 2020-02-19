package grakn.simulation.agents;

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

