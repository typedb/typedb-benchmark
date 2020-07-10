package grakn.simulation.agents.base;

import grakn.simulation.common.Pair;
import grakn.simulation.common.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

/**
 * AgentRunner constructs agents of a given class and runs them in parallel, providing them with the appropriate item,
 * a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <T> The type of item used by the agent.
 */
public abstract class AgentRunner<T> {

    private Constructor<? extends Agent<T>> agentConstructor;
    private Logger logger;
    private Boolean traceAgent = true;

    protected AgentRunner(Class<? extends Agent<T>> agentClass) {
        try {
            agentConstructor = agentClass.getDeclaredConstructor();
            agentConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        logger = LoggerFactory.getLogger(agentClass);
    }

    public void setTrace(Boolean trace) {
        traceAgent = trace;
    }

    abstract protected List<T> getParallelItems(IterationContext iterationContext, RandomSource randomSource);

    abstract protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, T item);

    abstract protected String getTracker(IterationContext iterationContext, RandomSource randomSource, T item);

    public void iterate(IterationContext iterationContext, RandomSource randomSource) {
        List<T> items = getParallelItems(iterationContext, randomSource);
        List<RandomSource> sources = randomSource.split(items.size());

        Pair.zip(sources, items).parallelStream().forEach(
                pair -> runAgent(iterationContext, pair.getFirst(), pair.getSecond())
        );
    }

    private void runAgent(IterationContext iterationContext, RandomSource source, T item) {
        Random random = source.startNewRandom();
        Random agentRandom = RandomSource.nextSource(random).startNewRandom();
        String sessionKey = getSessionKey(iterationContext, RandomSource.nextSource(random), item);
        String tracker = getTracker(iterationContext, RandomSource.nextSource(random), item);

        try (Agent<T> agent = agentConstructor.newInstance()) {

            agent.init(iterationContext, agentRandom, item, sessionKey, tracker, logger, traceAgent && iterationContext.shouldTrace());
            agent.iterate();

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
