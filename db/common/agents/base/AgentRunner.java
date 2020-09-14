package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.context.DatabaseContext;
import grakn.simulation.db.common.world.Region;
import grakn.simulation.utils.RandomSource;
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
 * @param <REGION> The type of region used by the agent.
 * @param <CONTEXT> The database context used by the agent.
 */
public abstract class AgentRunner<REGION extends Region, CONTEXT extends DatabaseContext> {

    private final Constructor<? extends Agent<REGION, CONTEXT>> agentConstructor;
    private final Logger logger;
    private Boolean traceAgent = true;
    private final CONTEXT backendContext;

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

    abstract protected List<REGION> getParallelItems(IterationContext iterationContext);

    abstract protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, REGION item);

    public void iterate(IterationContext iterationContext, RandomSource randomSource) {
        List<REGION> regions = getParallelItems(iterationContext);
        List<RandomSource> sources = randomSource.split(regions.size());

        Pair.zip(sources, regions).parallelStream().forEach(
                pair -> runAgent(iterationContext, pair.getFirst(), pair.getSecond())
        );
    }

    private void runAgent(IterationContext iterationContext, RandomSource source, REGION region) {
        Random random = source.startNewRandom();
        Random agentRandom = RandomSource.nextSource(random).startNewRandom();
        String sessionKey = getSessionKey(iterationContext, RandomSource.nextSource(random), region);

        try (Agent<REGION, CONTEXT> agent = agentConstructor.newInstance()) {
            agent.init(iterationContext.simulationStep(), agentRandom, backendContext, sessionKey, region.tracker(), logger, traceAgent && iterationContext.shouldTrace());
//            AgentResult agentResult = agent.iterateWithTracing();  // TODO Disabled for demo purposes
            AgentResultSet agentResult = agent.iterate(agent, region, iterationContext);
            iterationContext.getResultHandler().newResult(agent.getClass().getSimpleName(), region.tracker(), agentResult);
//            lastTestCount.put(tracker, agent.testByCount(lastTestCount.getOrDefault(tracker, 0)));

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
