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

    private SessionStrategy sessionStrategy;

    public enum SessionStrategy {
        CITY, COUNTRY, CONTINENT
    }

    public CityAgentRunner(Class<? extends Agent<World.City>> agentClass, SessionStrategy sessionStrategy) {
        super(agentClass);
        this.sessionStrategy = sessionStrategy;
    }

    @Override
    protected List<World.City> getParallelItems(IterationContext iterationContext, RandomSource randomSource) {
        return iterationContext.getWorld().getCities().collect(toList());
    }

    @Override
    protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, World.City city) {
        switch (sessionStrategy) {
            case CITY:
                return city.name();
            case COUNTRY:
                return city.country().name();
            case CONTINENT:
                return city.country().continent().name();
            default:
                throw new IllegalArgumentException("Unexpected session strategy: " + sessionStrategy.name());
        }
    }

    @Override
    protected String getTracker(IterationContext iterationContext, RandomSource randomSource, World.City city) {
        return Tracker.of(city.country().continent(), city.country(), city);
    }
}

