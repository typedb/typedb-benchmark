package grakn.simulation.agents.base;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadContext;
import grakn.simulation.agents.interaction.RandomValueGenerator;
import grakn.simulation.world.World;
import grakn.simulation.driver.DbDriverWrapper;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;

/**
 * An agent that performs some unit of work across an object in the simulation world. Agent definitions must extend
 * this class.
 *
 * This class is instantiated via reflection by {@link AgentRunner} and initialized using
 * {@link #init(IterationContext, Random, Object, String, String, Logger, Boolean)}.
 *
 * The protected methods of this class provide useful simple methods for writing Agents as concisely as possible.
 */
public abstract class Agent<T> implements AutoCloseable {

    private IterationContext iterationContext;
    private Random random;
    private T item;
    private String sessionKey;
    private String tracker;
    private LogWrapper logWrapper;
    private ThreadContext context;

    void init(IterationContext iterationContext, Random random, T item, String sessionKey, String tracker, Logger logger, Boolean trace) {
        this.iterationContext = iterationContext;
        this.random = random;
        this.item = item;
        this.sessionKey = sessionKey;
        this.tracker = tracker;

        this.logWrapper = new LogWrapper(logger);
        if (trace) {
            context = contextOnThread(tracker(), simulationStep());
        }
    }

    private DbDriverWrapper.Session.Transaction tx;
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

    protected T item() {
        return item;
    }

    protected String tracker() {
        return tracker;
    }

    protected DbDriverWrapper.Session session() {
        return iterationContext.getIterationSessionFor(getSessionKey());
    }

    protected DbDriverWrapper.Session.Transaction transaction() {
        return iterationContext.getIterationSessionFor(getSessionKey()).transaction();
    }

    protected String getSessionKey() {
        return sessionKey;
    }

    protected DbDriverWrapper.Session.Transaction tx() {
        if (tx == null) {
            tx = transaction();
        }
        return tx;
    }

    protected void closeTx() {
        if (tx != null) {
            tx.close();
            tx = null;
        }
    }

    protected void setTx(DbDriverWrapper.Session.Transaction tx) {
        closeTx();
        this.tx = tx;
    }

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
        closeTx();
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

    public abstract void iterate();

    public class LogWrapper {
        private final Logger logger;

        private LogWrapper(Logger logger) {
            this.logger = logger;
        }

        public void query(String scope, GraqlQuery query) {
            logger.info("({}):{}:\n{}", tracker, scope, query);
        }

        public void message(String message) {
            logger.info("({}):{}", tracker, message);
        }
    }
}
