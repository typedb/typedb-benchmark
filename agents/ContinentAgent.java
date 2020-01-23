package grakn.simulation.agents;

import grakn.simulation.agents.World.Continent;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface ContinentAgent extends ParallelAgent<Continent> {

    @Override
    default List<Continent> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getContinents().collect(toList());
    }
}
