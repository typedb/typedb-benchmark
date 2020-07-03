package grakn.simulation.agents.common;

import grakn.simulation.agents.World;
import grakn.simulation.agents.base.Agent;
import grakn.simulation.agents.base.AgentContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.Tracker;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CountryAgentRunner extends AgentRunner<World.Country> {

    public CountryAgentRunner(Class<? extends Agent<World.Country>> agentClass, Boolean trace) {
        super(agentClass, trace);
    }

    @Override
    protected List<World.Country> getParallelItems(AgentContext agentContext, RandomSource randomSource) {
        return agentContext.getWorld().getCountries().collect(toList());
    }

    @Override
    protected String getSessionKey(AgentContext agentContext, RandomSource randomSource, World.Country country) {
        return country.name();
    }

    @Override
    protected String getTracker(AgentContext agentContext, RandomSource randomSource, World.Country country) {
        return Tracker.of(country.continent(), country);
    }
}