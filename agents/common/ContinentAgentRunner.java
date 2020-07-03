package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.Tracker;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ContinentAgentRunner extends AgentRunner<World.Continent> {

    public ContinentAgentRunner(Class<? extends Agent<World.Continent>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<World.Continent> getParallelItems(AgentContext agentContext, RandomSource randomSource) {
        return agentContext.getWorld().getContinents().collect(toList());
    }

    @Override
    protected String getSessionKey(AgentContext agentContext, RandomSource randomSource, World.Continent continent) {
        return continent.name();
    }

    @Override
    protected String getTracker(AgentContext agentContext, RandomSource randomSource, World.Continent continent) {
        return Tracker.of(continent);
    }
}
