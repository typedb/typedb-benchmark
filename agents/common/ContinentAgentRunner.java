package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.IterationContext;
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
    protected List<World.Continent> getParallelItems(IterationContext iterationContext, RandomSource randomSource) {
        return iterationContext.getWorld().getContinents().collect(toList());
    }

    @Override
    protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, World.Continent continent) {
        return continent.name();
    }

    @Override
    protected String getTracker(IterationContext iterationContext, RandomSource randomSource, World.Continent continent) {
        return Tracker.of(continent);
    }
}
