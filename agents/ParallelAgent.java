package grakn.simulation.agents;

import grakn.simulation.common.Pair;
import grakn.simulation.common.RandomSource;

import java.util.List;

public interface ParallelAgent<T> extends Agent {

    void iterate(AgentContext context, RandomSource randomSource, T item);

    List<T> getParallelItems(AgentContext context);

    @Override
    default void iterate(AgentContext context, RandomSource randomSource) {

        List<T> items = getParallelItems(context);
        List<RandomSource> sources = randomSource.split(items.size());

        Pair.zip(sources, items).parallelStream().forEach(
                pair -> iterate(context, pair.getFirst(), pair.getSecond())
        );
    }
}
