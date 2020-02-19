package grakn.simulation.agents;

import grakn.simulation.agents.World.Continent;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ContinentAgentRunner extends AgentRunner<Continent> {

    protected ContinentAgentRunner(Class<? extends Agent<Continent>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<Continent> getParallelItems(AgentContext agentContext) {
        return agentContext.getWorld().getContinents().collect(toList());
    }
}
