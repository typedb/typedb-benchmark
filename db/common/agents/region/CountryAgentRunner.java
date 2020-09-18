package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.utils.RandomSource;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CountryAgentRunner<CONTEXT extends DatabaseContext> extends AgentRunner<World.Country, CONTEXT> {

    private final SessionStrategy sessionStrategy;

    public enum SessionStrategy {
        COUNTRY, CONTINENT
    }

    public CountryAgentRunner(Class<? extends Agent<World.Country, CONTEXT>> agentClass, CONTEXT backendContext, SessionStrategy sessionStrategy) {
        super(agentClass, backendContext);
        this.sessionStrategy = sessionStrategy;
    }

    @Override
    protected List<World.Country> getParallelItems(IterationContext iterationContext) {
        return iterationContext.world().getCountries().collect(toList());
    }

    @Override
    protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, World.Country country) {
        switch (sessionStrategy) {
            case COUNTRY:
                return country.name();
            case CONTINENT:
                return country.continent().name();
            default:
                throw new IllegalArgumentException("Unexpected session strategy: " + sessionStrategy.name());
        }
    }
}