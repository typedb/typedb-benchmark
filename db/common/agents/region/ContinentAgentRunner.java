package grakn.simulation.db.common.agents.region;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.SimulationContext;
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
    protected List<World.Continent> getParallelItems(SimulationContext simulationContext) {
        return simulationContext.world().getContinents().collect(toList());
    }

    @Override
    protected String getSessionKey(SimulationContext simulationContext, RandomSource randomSource, World.Continent continent) {
        return continent.name();
    }
}
