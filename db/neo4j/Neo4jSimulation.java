package grakn.simulation.db.neo4j;

import grakn.simulation.config.Config;
import grakn.simulation.db.common.Simulation;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.neo4j.context.Neo4jContext;
import grakn.simulation.db.neo4j.initialise.Neo4jAgentPicker;
import grakn.simulation.db.neo4j.initialise.Neo4jInitialiser;
import grakn.simulation.utils.RandomSource;
import org.neo4j.driver.Session;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Neo4jSimulation extends Simulation<Neo4jContext> {

    private Neo4jContext neo4jContext;

    public Neo4jSimulation(String hostUri, Map<String, Path> initialisationDataPaths, RandomSource randomSource, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, ResultHandler resultHandler) {
        super(hostUri, null, initialisationDataPaths, randomSource, world, agentConfigs, iterationSamplingFunction, resultHandler);
    }

    @Override
    protected void setBackendContext(String hostUri, String database) {
        neo4jContext = new Neo4jContext(hostUri);
    }

    @Override
    protected AgentPicker<Neo4jContext> getAgentPicker() {
        return new Neo4jAgentPicker(neo4jContext);
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        Session session = neo4jContext.session("initialise");
        new Neo4jInitialiser(session, initialisationDataPaths).initialise();
    }

    @Override
    protected void closeIteration() {
        neo4jContext.closeSessions();
    }

    @Override
    public void close() {
        neo4jContext.close();
    }
}
