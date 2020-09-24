package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.utils.RandomSource;
import grakn.simulation.db.common.world.World;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CityAgentRunner<CONTEXT extends DatabaseContext> extends AgentRunner<World.City, CONTEXT> {

    private SessionStrategy sessionStrategy;

    public enum SessionStrategy {
        CITY, COUNTRY, CONTINENT
    }

    public CityAgentRunner(Class<? extends Agent<World.City, CONTEXT>> agentClass, CONTEXT backendContext, SessionStrategy sessionStrategy) {
        super(agentClass, backendContext);
        this.sessionStrategy = sessionStrategy;
    }

    @Override
    protected List<World.City> getParallelItems(SimulationContext simulationContext) {
        return simulationContext.world().getCities().collect(toList());
    }

    @Override
    protected String getSessionKey(SimulationContext simulationContext, RandomSource randomSource, World.City city) {
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
}

