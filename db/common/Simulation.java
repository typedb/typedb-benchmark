package grakn.simulation.db.common;

import grakn.simulation.config.Config;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.agent.interaction.AgentFactory;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Simulation<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> implements SimulationContext {

    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private final List<Agent<?, DB_DRIVER, DB_OPERATION>> agentList;
    protected final DB_DRIVER driver;
    private final Random random;
    private final List<Config.Agent> agentConfigs;
    private final Function<Integer, Boolean> iterationSamplingFunction;
    private final Report report = new Report();
    private final World world;
    private final boolean test;
    private int simulationStep = 1;

    public Simulation(DB_DRIVER driver, Map<String, Path> initialisationDataPaths, RandomSource randomSource, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        this.driver = driver;
        this.random = randomSource.startNewRandom();
        this.agentConfigs = agentConfigs;
        this.iterationSamplingFunction = iterationSamplingFunction;
        this.world = world;
        this.test = test;
        initialise(initialisationDataPaths);
        this.agentList = agentListFromConfigs();
    }

    protected List<Agent<?, DB_DRIVER, DB_OPERATION>> agentListFromConfigs() {
        List<Agent<?, DB_DRIVER, DB_OPERATION>> agents = new ArrayList<>();
        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                Agent<?, DB_DRIVER, DB_OPERATION> agent = new AgentFactory<>(driver, actionFactory()).get(agentConfig.getName());
                agent.setTrace(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    protected abstract ActionFactory<DB_OPERATION, ?> actionFactory();

    protected abstract void initialise(Map<String, Path> initialisationDataPaths);

    public Report iterate() {

        LOG.info("Simulation step: {}", simulationStep);
        report.clean();
        for (Agent<?, DB_DRIVER, DB_OPERATION> agent : agentList) {
            this.report.addAgentResult(agent.getClass().getName(), agent.iterate(this, RandomSource.nextSource(random)));
        }
        closeIteration();  // We want to test opening new sessions each iteration.
        simulationStep++;
        return report;
    }

    protected abstract void closeIteration();

    @Override
    public int simulationStep() {
        return simulationStep;
    }

    @Override
    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(simulationStep, 1), LocalTime.of(0, 0, 0));
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public boolean trace() {
        return iterationSamplingFunction.apply(simulationStep());
    }

    @Override
    public boolean test() {
        return test;
    }

    public Report getReport() {
        return report;
    }

    public abstract void close();

    public class Report {

        private ConcurrentHashMap<String, Agent<?, DB_DRIVER, ?>.Report> agentReports = new ConcurrentHashMap<>();

        public void addAgentResult(String agentName, Agent<?, DB_DRIVER, ?>.Report agentReport) {
            if (agentReport == null) {
                throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
            }
            agentReports.put(agentName, agentReport);
        }

        public Agent<?, DB_DRIVER, ?>.Report getAgentReport(String agentName) {
            return agentReports.get(agentName);
        }

        public void clean() {
            agentReports = new ConcurrentHashMap<>();
        }
    }
}
