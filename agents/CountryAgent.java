package grakn.simulation.agents;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface CountryAgent extends ParallelAgent<World.Country> {

    @Override
    default List<World.Country> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getCountries().collect(toList());
    }
}