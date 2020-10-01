package grakn.simulation.db.common.agents.base;

import grabl.tracing.client.GrablTracingThreadStatic;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadContext;
import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.common.agents.interaction.InteractionAgent;
import grakn.simulation.db.common.agents.interaction.RandomValueGenerator;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.context.LogWrapper;
import grakn.simulation.db.common.world.Region;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

/**
 * An agent that performs some unit of work across an object in the simulation world. Agent definitions must extend
 * this class.
 *
 * This class is instantiated via reflection by {@link AgentRunner} and initialized using
 * {@link #init(SimulationContext, Random, Object, Object, String, String, Logger, Boolean)}.
 *
 * The protected methods of this class provide useful simple methods for writing Agents as concisely as possible.
 */
public abstract class Agent<CONTEXT extends DatabaseContext<?>> implements InteractionAgent<Region>, AutoCloseable {

    private Random random;
    private CONTEXT backendContext;
    private String sessionKey;
    private String tracker;
    private LogWrapper logWrapper;
    private Boolean testing;
    private ThreadContext context;
    private HashSet<String> tracedMethods = new HashSet<>();
    protected Action<?, ?> action;
    private HashMap<String, ArrayList<Action.Report>> actionResults = new HashMap<>();

    void init(int simulationStep, Random random, CONTEXT backendContext, String sessionKey, String tracker, Logger logger, Boolean trace, Boolean test) {
        this.random = random;
        this.backendContext = backendContext;
        this.sessionKey = sessionKey;
        this.tracker = tracker;
        this.logWrapper = new LogWrapper(logger);
        this.testing = test;
        if (trace) {
            context = contextOnThread(tracker(), simulationStep);
        }
    }

    public LogWrapper logger() {
        return logWrapper;
    }

    public RandomValueGenerator randomAttributeGenerator() {
        return new RandomValueGenerator(random);
    }

    protected CONTEXT backendContext() {
        return backendContext;
    }

    public String tracker() {
        return tracker;
    }

    protected String getSessionKey() {
        return sessionKey;
    }

    protected abstract void startDbOperation(Action<?, ?> action);

    protected abstract void closeDbOperation();

    protected abstract void saveDbOperation();

    public abstract class DbOperation implements AutoCloseable {
        public abstract void close();
        public abstract void commit();
    }

    public abstract DbOperation dbOperation(Action<?, ?> action);

    /////////////////////////////////////////////////
    // Helper methods called from agent interfaces //
    /////////////////////////////////////////////////

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
    public int uniqueId(SimulationContext simulationContext, int iterationScopeId) {
        String id = simulationContext.simulationStep() + tracker() + iterationScopeId;
        return id.hashCode();
    }

    /////////////////////////////////////////////////
    // Action methods called from agent interfaces //
    /////////////////////////////////////////////////

    @Override
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    public Action<?, ?> action() {
        return action;
    }

    public abstract ActionFactory<?, ?> actionFactory();

    public HashMap<String, ArrayList<Action.Report>> getActionResults() {
        return actionResults;
    }

    public <ACTION_RETURN_TYPE> ACTION_RETURN_TYPE runAction(Action<?, ACTION_RETURN_TYPE> action) {
        ACTION_RETURN_TYPE actionAnswer;
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(action.name())) {
            actionAnswer = action.run();
        }
        if (testing) {
            Action.Report actionResult = action.report(actionAnswer);
//            if (!action.resultsOptional() && actionResult.size() == 0) {
//                throw new RuntimeException();
//            }
            actionResults.computeIfAbsent(action.name(), x -> new ArrayList<>()).add(actionResult);
        }
        return actionAnswer;
    }

}
