package grakn.simulation.agents;

import grakn.simulation.common.Pair;
import grakn.simulation.common.RandomSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public abstract class AgentRunner<T extends AgentItem> {

    private Constructor<? extends Agent<T>> agentConstructor;

    protected AgentRunner(Class<? extends Agent<T>> agentClass) {
        try {
            agentConstructor = agentClass.getDeclaredConstructor();
            agentConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Agent<T> constructAgent(AgentContext context, Random random, T item) {
        try {
            Agent<T> agent = agentConstructor.newInstance();
            agent.init(context, random, item);
            return agent;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected List<T> getParallelItems(AgentContext agentContext);

    public void iterate(AgentContext agentContext, RandomSource randomSource) {
        List<T> items = getParallelItems(agentContext);
        List<RandomSource> sources = randomSource.split(items.size());

        Pair.zip(sources, items).parallelStream().forEach(
                pair -> {
                    try (Agent<T> agent = constructAgent(
                            agentContext,
                            pair.getFirst().startNewRandom(),
                            pair.getSecond()
                    )) {
                        agent.iterate();
                    }
                }
        );
    }
}
