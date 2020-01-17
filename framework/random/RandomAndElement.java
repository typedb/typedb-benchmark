package grakn.simulation.framework.random;

public interface RandomAndElement<T> {
    T getItem();

    RandomSource getRandomSource();
}
