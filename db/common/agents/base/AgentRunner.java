package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.utils.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * AgentRunner constructs agents of a given class and runs them in parallel, providing them with the appropriate item,
 * a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION> The type of region used by the agent.
 * @param <CONTEXT> The database context used by the agent.
 */
public abstract class AgentRunner<REGION extends Region, CONTEXT extends DatabaseContext> {

    private Constructor<? extends Agent<REGION, CONTEXT>> agentConstructor;
    private Logger logger;
    private Boolean traceAgent = true;
    private HashMap<String, Integer> lastTestCount = new HashMap<>();
    private CONTEXT backendContext;

    protected AgentRunner(Class<? extends Agent<REGION, CONTEXT>> agentClass, CONTEXT backendContext) {
        this.backendContext = backendContext;
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

    abstract protected List<REGION> getParallelItems(IterationContext iterationContext, RandomSource randomSource);

    abstract protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, REGION item);

    abstract protected String getTracker(IterationContext iterationContext, RandomSource randomSource, REGION item);

    public void iterate(IterationContext iterationContext, RandomSource randomSource) {
        List<REGION> items = getParallelItems(iterationContext, randomSource);
        List<RandomSource> sources = randomSource.split(items.size());

        Pair.zip(sources, items).parallelStream().forEach(
                pair -> runAgent(iterationContext, pair.getFirst(), pair.getSecond())
        );
    }

    private void runAgent(IterationContext iterationContext, RandomSource source, REGION worldLocality) {
        Random random = source.startNewRandom();
        Random agentRandom = RandomSource.nextSource(random).startNewRandom();
        String sessionKey = getSessionKey(iterationContext, RandomSource.nextSource(random), worldLocality);
        String tracker = getTracker(iterationContext, RandomSource.nextSource(random), worldLocality);

        try (Agent<REGION, CONTEXT> agent = agentConstructor.newInstance()) {
            agent.init(iterationContext, agentRandom, worldLocality, backendContext, sessionKey, tracker, logger, traceAgent && iterationContext.shouldTrace());
//            AgentResult agentResult = agent.iterateWithTracing();  // TODO Disabled for demo purposes
            AgentResultSet agentResult = agent.iterate();
            iterationContext.getResultHandler().newResult(agent.getClass().getSimpleName(), tracker, agentResult);
//            lastTestCount.put(tracker, agent.testByCount(lastTestCount.getOrDefault(tracker, 0)));

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
