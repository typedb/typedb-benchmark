package grakn.simulation.db.grakn;

import grakn.client.GraknClient.Session;
import grakn.simulation.config.Config;
import grakn.simulation.db.common.Simulation;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;
import grakn.simulation.db.grakn.initialise.GraknAgentPicker;
import grakn.simulation.db.grakn.initialise.GraknInitialiser;
import grakn.simulation.utils.RandomSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GraknSimulation extends Simulation<GraknContext, Transaction> {

    private GraknContext graknContext;

    public GraknSimulation(String hostUri, String database, Map<String, Path> initialisationDataPaths, RandomSource randomSource, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, ResultHandler resultHandler, boolean test) {
        super(hostUri, database, initialisationDataPaths, randomSource, world, agentConfigs, iterationSamplingFunction, resultHandler, test);
    }

    @Override
    protected void setBackendContext(String hostUri, String database) {
        graknContext = new GraknContext(hostUri, database);
    }

    @Override
    protected AgentPicker<GraknContext> getAgentPicker() {
        return new GraknAgentPicker(graknContext);
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        Session session = graknContext.session("initialise");
        new GraknInitialiser(session, initialisationDataPaths).initialise(); //TODO seems like an anti-pattern
        graknContext.closeSessions();
    }

    @Override
    protected void closeIteration() {
        graknContext.closeSessions();
    }

    @Override
    public void close() {
        graknContext.close();
    }
}
