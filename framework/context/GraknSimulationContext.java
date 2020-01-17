package grakn.simulation.framework.context;

import grakn.client.GraknClient;
import grakn.simulation.framework.random.RandomSource;

import java.util.List;
import java.util.stream.Collectors;

public class GraknSimulationContext implements SimulationContext {

    private GraknClient.Session graknSession;
    private RandomSource randomSource;

    public GraknSimulationContext(GraknClient.Session graknSession, RandomSource randomSource) {
        this.graknSession = graknSession;
        this.randomSource = randomSource;
    }

    @Override
    public RandomSource getRandomSource() {
        return randomSource;
    }

    @Override
    public GraknClient.Session getGraknSession() {
        return graknSession;
    }

    @Override
    public <T> List<SimulationContextAndElement<T>> split(List<T> list) {
        return randomSource.split(list).stream()
                .map(e -> new SimulationContextAndElementImpl<>(
                        new GraknSimulationContext(graknSession, e.getRandomSource()),
                        e.getItem()
                )).collect(Collectors.toList());
    }
}
