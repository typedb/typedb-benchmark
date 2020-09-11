package grakn.simulation.db.neo4j;

import grakn.simulation.db.common.Simulation;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.world.World;
import grakn.simulation.utils.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Neo4jSimulation extends Simulation {
    final static Logger LOG = LoggerFactory.getLogger(Neo4jSimulation.class);

    public Neo4jSimulation(String hostUri, Map<String, Path> initialisationDataPaths, RandomSource randomSource, World world, List<AgentRunner<?, ?>> agentRunnerList, Function<Integer, Boolean> iterationSamplingFunction, ResultHandler resultHandler) {
        super(initialisationDataPaths, randomSource, world, agentRunnerList, iterationSamplingFunction, resultHandler);
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {

    }

    @Override
    public void close() {

    }
}
