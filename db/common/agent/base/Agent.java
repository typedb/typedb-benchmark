package grakn.simulation.db.common.agent.base;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.utils.Pair;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.db.common.world.World;
import grakn.simulation.utils.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION> The type of region used by the agent.
 * @param <DB_DRIVER> The database context used by the agent.
 */
// TODO: refactor out DB_DRIVER, replace with DbDriver<DB_OPERATION>
public abstract class Agent<REGION extends Region, DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> {

    private final Logger logger;
    private boolean traceAgent = true;
    private final DB_DRIVER dbDriver;
    private final ActionFactory<DB_OPERATION, ?> actionFactory;
    private final Report report = new Report();

    protected Agent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected ActionFactory<DB_OPERATION, ?> actionFactory() {
        return actionFactory;
    }

    public void setTrace(boolean trace) {
        traceAgent = trace;
    }

    abstract protected List<REGION> getRegions(World world);

    public Report iterate(SimulationContext simulationContext, RandomSource randomSource) {
        List<REGION> regions = getRegions(simulationContext.world());
        List<RandomSource> randomSources = randomSource.split(regions.size());

        Pair.zip(randomSources, regions).parallelStream().forEach(
                pair -> iterateRegionalAgent(simulationContext, pair.getFirst(), pair.getSecond())
        );
        return report;
    }

    protected abstract RegionalAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test);

    private void iterateRegionalAgent(SimulationContext simulationContext, RandomSource source, REGION region) {
        Random random = source.startNewRandom();
        Random agentRandom = RandomSource.nextSource(random).startNewRandom();

        RegionalAgent regionalAgent = getRegionalAgent(simulationContext.simulationStep(), region.tracker(), agentRandom, simulationContext.test());
        DbOperationFactory<DB_OPERATION> dbOperationFactory = dbDriver.getDbOperationFactory(region, logger);

        RegionalAgent.Report report = regionalAgent.runWithReport(dbOperationFactory, region, simulationContext);
        this.report.addRegionalAgentReport(region.tracker(), report);
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public class Report {
        ConcurrentHashMap<String, RegionalAgent.Report> regionalAgentReports = new ConcurrentHashMap<>();

        public void addRegionalAgentReport(String tracker, RegionalAgent.Report regionalAgentReport) {
            regionalAgentReports.put(tracker, regionalAgentReport);
        }

        public Set<String> trackers() {
            return regionalAgentReports.keySet();
        }

        public RegionalAgent.Report getRegionalAgentReport(String tracker) {
            return regionalAgentReports.get(tracker);
        }
    }


    // TODO: Rename this to Regional, as well as the other Regional agents
    public abstract class RegionalAgent implements AutoCloseable {

        private final Random random;
        private final boolean test;
        private final Report report = new Report();
        private final String tracker;
        private GrablTracingThreadStatic.ThreadContext context;

        public RegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
            this.tracker = tracker;
            this.random = random;
            this.test = test;
            if (traceAgent) {
                context = contextOnThread(tracker(), simulationStep);
            }
        }

        public String tracker() {
            return tracker;
        }

        protected Report runWithReport(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region, SimulationContext simulationContext) {
            run(dbOperationFactory, region, simulationContext);
            return report;
        }

        protected abstract void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region, SimulationContext simulationContext);

        void runWithTracing(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region, SimulationContext simulationContext) {
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(this.name())) {
                runWithReport(dbOperationFactory, region, simulationContext);
            }
        }

        public String name() {
            return this.getClass().getSimpleName();
        }

        public <U> U pickOne(List<U> list) { // TODO can be a util
            return list.get(random().nextInt(list.size()));
        }

        public Random random() {
            return random;
        }

        protected void shuffle(List<?> list) {
            Collections.shuffle(list, random());
        }

        /**
         * Create a unique identifier, useful for creating keys without risk of collision
         * @param iterationScopeId An id that uniquely identifies a concept within the scope of the agent at a particular iteration
         * @return
         */
        public String uniqueId(SimulationContext simulationContext, int iterationScopeId) {
            return simulationContext.simulationStep() + "/" + tracker() + "/" + iterationScopeId;
        }

        public RandomValueGenerator randomAttributeGenerator() {
            return new RandomValueGenerator(random);
        }

        public <ACTION_RETURN_TYPE> ACTION_RETURN_TYPE runAction(Action<?, ACTION_RETURN_TYPE> action) {
            ACTION_RETURN_TYPE actionAnswer;
            try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(action.name())) {
                actionAnswer = action.run();
            }
            if (test) {
                report.addActionReport(action.name(), action.report(actionAnswer));
            }
            return actionAnswer;
        }

        public class Report {
            HashMap<String, ArrayList<Action<?, ?>.Report>> actionReports = new HashMap<>();

            public void addActionReport(String actionName, Action<?, ?>.Report actionReport) {
                actionReports.computeIfAbsent(actionName, x -> new ArrayList<>()).add(actionReport);
            }

            public Set<String> actionNames() {
                return actionReports.keySet();
            }

            public ArrayList<Action<?, ?>.Report> getActionReport(String actionName) {
                return actionReports.get(actionName);
            }
        }

        @Override
        public void close() {
            if (context != null) {
                context.close();
            }
        }
    }
}
