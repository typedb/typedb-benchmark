package grakn.simulation.agents.world;

import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.IterationContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.Tracker;
import grakn.simulation.world.World;

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

