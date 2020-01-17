package grakn.simulation.framework.random;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class RandomSourceImpl implements RandomSource {
    private final long seed;

    RandomSourceImpl(long seed) {
        this.seed = seed;
    }

    @Override
    public <T> List<RandomAndElement<T>> split(List<T> items) {
        Random random = start();
        return items.stream().map(i -> new RandomAndElementImpl<>(RandomSource.next(random), i))
                .collect(Collectors.toList());
    }

    @Override
    public Random start() {
        return new Random(seed);
    }

    @Override
    public Random at(long position) {
        Random random = start();
        for (long i = 0; i < position; ++i) {
            random.nextLong();
        }
        return random;
    }
}
