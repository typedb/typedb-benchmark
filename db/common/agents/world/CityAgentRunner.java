package grakn.simulation.db.common.agents.world;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.utils.RandomSource;
import grakn.simulation.db.common.agents.utils.Tracker;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CityAgentRunner extends AgentRunner<World.City> {

    public CityAgentRunner(Class<? extends Agent<World.City>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<World.City> getParallelItems(IterationContext iterationContext, RandomSource randomSource) {
        return iterationContext.getWorld().getCities().collect(toList());
    }

    @Override
    protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, World.City city) {
        return city.country().continent().name();
    }

    @Override
    protected String getTracker(IterationContext iterationContext, RandomSource randomSource, World.City city) {
        return Tracker.of(city.country().continent(), city.country(), city);
    }
}

