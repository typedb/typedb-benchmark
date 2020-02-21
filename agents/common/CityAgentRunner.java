package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.Tracker;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CityAgentRunner extends AgentRunner<World.City> {

    public CityAgentRunner(Class<? extends Agent<World.City>> agentClass) {
        super(agentClass);
    }

    @Override
    protected List<World.City> getParallelItems(AgentContext agentContext, RandomSource randomSource) {
        return agentContext.getWorld().getCities().collect(toList());
    }

    @Override
    protected String getSessionKey(AgentContext agentContext, RandomSource randomSource, World.City city) {
        return city.country().continent().name();
    }

    @Override
    protected String getTracker(AgentContext agentContext, RandomSource randomSource, World.City city) {
        return Tracker.of(city.country().continent(), city.country(), city);
    }
}

