package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.utils.Tracker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.utils.RandomSource;

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
