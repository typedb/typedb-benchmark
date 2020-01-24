package grakn.simulation.agents;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface CityAgent extends ParallelAgent<World.City> {

    @Override
    default List<World.City> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getCities().collect(toList());
    }
}

