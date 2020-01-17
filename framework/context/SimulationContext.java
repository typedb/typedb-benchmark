package grakn.simulation.framework.context;

import grakn.client.GraknClient;
import grakn.simulation.framework.random.RandomSource;

import java.util.List;

public interface SimulationContext {

    RandomSource getRandomSource();
    GraknClient.Session getGraknSession();

    <T> List<SimulationContextAndElement<T>> split(List<T> list);
}
