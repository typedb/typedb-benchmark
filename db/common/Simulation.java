package grakn.simulation.db.common;

import grakn.simulation.config.Config;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.utils.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public abstract class Simulation<CONTEXT extends DatabaseContext> implements IterationContext {

    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private final List<AgentRunner<?, CONTEXT>> agentRunnerList;
    private final Random random;
    private final List<Config.Agent> agentConfigs;
    private final Function<Integer, Boolean> iterationSamplingFunction;
    private final ResultHandler resultHandler;
    private final World world;
    private int simulationStep = 1;

    public Simulation(String hostUri, String database, Map<String, Path> initialisationDataPaths, RandomSource randomSource, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, ResultHandler resultHandler) {
        this.random = randomSource.startNewRandom();
        this.agentConfigs = agentConfigs;
        this.iterationSamplingFunction = iterationSamplingFunction;
        this.resultHandler = resultHandler;
        this.world = world;
        setBackendContext(hostUri, database);
        initialise(initialisationDataPaths);
        this.agentRunnerList = agentRunnerListFromConfigs();
    }

    protected abstract void setBackendContext(String hostUri, String database);

    protected abstract AgentPicker<CONTEXT> getAgentPicker();

    protected List<AgentRunner<?, CONTEXT>> agentRunnerListFromConfigs() {
        // Get the agents with their runners
        List<AgentRunner<?, CONTEXT>> agentRunners = new ArrayList<>();
        for (Config.Agent agent : agentConfigs) {
            if (agent.getAgentMode().getRun()) {
                AgentRunner<?, CONTEXT> runner = getAgentPicker().get(agent.getName());
                runner.setTrace(agent.getAgentMode().getTrace());
                agentRunners.add(runner);
            }
        }
        return agentRunners;
    }

    protected abstract void initialise(Map<String, Path> initialisationDataPaths);

    public ResultHandler iterate() {

        LOG.info("Simulation step: {}", simulationStep);
        resultHandler.clean();
        for (AgentRunner<?, CONTEXT> agentRunner : agentRunnerList) {
            agentRunner.iterate(this, RandomSource.nextSource(random));
        }

        closeIteration();  // We want to test opening new sessions each iteration.

        simulationStep++;
        return resultHandler;
    }

    protected abstract void closeIteration();

    @Override
    public int getSimulationStep() {
        return simulationStep;
    }

    @Override
    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(LocalDate.ofYearDay(simulationStep, 1), LocalTime.of(0, 0, 0));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean shouldTrace() {
        return iterationSamplingFunction.apply(getSimulationStep());
    }

    @Override
    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    public abstract void close();
}
