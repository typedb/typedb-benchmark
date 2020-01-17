package grakn.simulation.framework.random;

class RandomAndElementImpl<T> implements RandomAndElement<T> {
    private final RandomSource randomSource;
    private final T item;

    RandomAndElementImpl(RandomSource randomSource, T item) {
        this.randomSource = randomSource;
        this.item = item;
    }

    @Override
    public T getItem() {
        return item;
    }

    @Override
    public RandomSource getRandomSource() {
        return randomSource;
    }
}