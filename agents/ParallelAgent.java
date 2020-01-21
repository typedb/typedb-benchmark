package grakn.simulation.agents;

import grakn.client.GraknClient.Session;
import grakn.simulation.common.Pair;
import grakn.simulation.common.RandomSource;

import java.util.List;

public interface ParallelAgent<T> extends Agent {

    void iterate(Session session, RandomSource randomSource, T item);

    List<T> getParallelItems(Session session);

    @Override
    default void iterate(Session session, RandomSource randomSource) {

        List<T> items = getParallelItems(session);
        List<RandomSource> sources = randomSource.split(items.size());

        Pair.zip(sources, items).parallelStream().forEach(pair -> {
            iterate(session, pair.getFirst(), pair.getSecond());
        });
    }
}
