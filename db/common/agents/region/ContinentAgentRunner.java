package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.utils.Tracker;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.World;
import grakn.simulation.utils.RandomSource;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ContinentAgentRunner<CONTEXT extends DatabaseContext> extends AgentRunner<World.Continent, CONTEXT> {

    public ContinentAgentRunner(Class<? extends Agent<World.Continent, CONTEXT>> agentClass, CONTEXT backendContext) {
        super(agentClass, backendContext);
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
