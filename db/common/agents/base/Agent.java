package grakn.simulation.db.common.agents.base;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadContext;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.interaction.RandomValueGenerator;
import grakn.simulation.db.common.agents.utils.CheckMethod;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.world.World;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

/**
 * An agent that performs some unit of work across an object in the simulation world. Agent definitions must extend
 * this class.
 *
 * This class is instantiated via reflection by {@link AgentRunner} and initialized using
 * {@link #init(IterationContext, Random, Object, Object, String, String, Logger, Boolean)}.
 *
 * The protected methods of this class provide useful simple methods for writing Agents as concisely as possible.
 */
public abstract class Agent<T, C> implements AutoCloseable {

    protected IterationContext iterationContext;
    private Random random;
    private T worldLocality;
    private C backendContext;
    private String sessionKey;
    private String tracker;
    private LogWrapper logWrapper;
    private ThreadContext context;
    private HashSet<String> tracedMethods = new HashSet<>();

    void init(IterationContext iterationContext, Random random, T worldLocality, C backendContext, String sessionKey, String tracker, Logger logger, Boolean trace) {
        this.iterationContext = iterationContext;
        this.random = random;
        this.worldLocality = worldLocality;
        this.backendContext = backendContext;
        this.sessionKey = sessionKey;
        this.tracker = tracker;

        this.logWrapper = new LogWrapper(logger);
        if (trace) {
            context = contextOnThread(tracker(), simulationStep());
        }
    }

    private LocalDateTime today;

    protected LogWrapper log() {
        return logWrapper;
    }

    protected Random random() {
        return random;
    }

    protected RandomValueGenerator randomAttributeGenerator() {
        return new RandomValueGenerator(random());
    }

    protected T worldLocality() {
        return worldLocality;
    }

    protected C backendContext() {
        return backendContext;
    }

    protected String tracker() {
        return tracker;
    }

    protected String getSessionKey() {
        return sessionKey;
    }

//    protected DriverWrapper.Session.Transaction tx() {
//        if (tx == null) {
//            tx = iterationContext.getIterationSessionFor(getSessionKey()).transactionWithTracing();
//        }
//        return tx;
//    }

//    protected void closeTx() {
//        if (tx != null) {
//            tx.close();
//            tx = null;
//        }
//    }

    protected abstract void openTx();

    protected abstract void closeTx();

    protected abstract void commitTx();
//    {
//        tx.commitWithTracing();
//        tx = null;
//    }

//    protected void setTx(DriverWrapper.Session.Transaction tx) {
//        closeTx();
//        this.tx = tx;
//    }

    protected World world() {
        return iterationContext.getWorld();
    }

    protected LocalDateTime today() {
        if (today == null) {
            today = iterationContext.getLocalDateTime();
        }
        return today;
    }

    protected int simulationStep() {
        return iterationContext.getSimulationStep();
    }

    protected void shuffle(List<?> list) {
        Collections.shuffle(list, random);
    }

    protected <U> U pickOne(List<U> list) {
        return list.get(random().nextInt(list.size()));
    }

    @Override
    public void close() {
//        closeTx();
        if (context != null) {
            context.close();
        }
    }

    /**
     * Create a unique identifier, useful for creating keys without risk of collision
     * @param iterationScopeId An id that uniquely identifies a concept within the scope of the agent at a particular iteration
     * @return
     */
    protected int uniqueId(int iterationScopeId) {
        String id = simulationStep() + tracker() + iterationScopeId;
        return id.hashCode();
    }

    String name() {
        return this.getClass().getSimpleName();
    }

    AgentResultSet iterateWithTracing() {
        try (ThreadTrace trace = traceOnThread(name())) {
            System.out.println(name());
            return iterate();
        }
    }

    public abstract AgentResultSet iterate();

    public class LogWrapper {
        private final Logger logger;

        private LogWrapper(Logger logger) {
            this.logger = logger;
        }

        public void query(String scope, Object query) {
            query(scope, query.toString());
        }

        public void query(String scope, String query) {
            logger.info("({}):{}:\n{}", tracker, scope, query);
        }

        public void message(String scope, String message) {
            logger.info("({}):{}:\n{}", tracker, scope, message);
        }
    }

    protected String registerMethodTrace(String methodName) {
        CheckMethod.checkMethodExists(this, methodName);
        if (tracedMethods.contains(methodName)) {
            throw new RuntimeException(String.format("Method %s has already been registered for tracing for class %s", methodName, this.getClass().getName()));
        }
        tracedMethods.add(methodName);
        return methodName;
    }

    protected String checkMethodTrace(String methodName) {
        CheckMethod.checkMethodExists(this, methodName);
        return methodName;
    }

    public interface ComparableField {}

    protected abstract int checkCount();

    protected abstract Pair<Integer, Integer> countBounds();

    int testByCount(int previousCount) {
        Integer testCountLowerBound = countBounds().getFirst();
        Integer testCountUpperBound = countBounds().getSecond();

        if (testCountUpperBound == null) {
            throw new RuntimeException(String.format("An upper bound has not been set for the answer count for agent %s", this.getClass().getName()));
        }
        if (testCountLowerBound == null) {
            throw new RuntimeException(String.format("A lower bound has not been set for the answer count for agent %s", this.getClass().getName()));
        }
        int count = checkCount();
        int newlyInserted = count - previousCount;
        log().message(this.getClass().getSimpleName(), String.format("iteration count diff: %d", newlyInserted));
        if (newlyInserted < testCountLowerBound) {
            throw new RuntimeException(String.format("Testing found that there were fewer results than expected for agent %s. Expected %d or more, found %d", this.getClass().getName(), testCountLowerBound, newlyInserted));
        } else if (newlyInserted > testCountUpperBound) {
            throw new RuntimeException(String.format("Testing found that there were more results than expected for agent %s. Expected up to %d, found %d", this.getClass().getName(), testCountUpperBound, newlyInserted));
        }
        return count;
    }
}
