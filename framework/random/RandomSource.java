package grakn.simulation.framework.random;

import java.util.List;
import java.util.Random;

public interface RandomSource {
    <T> List<RandomAndElement<T>> split(List<T> items);

    Random start();

    Random at(long position);

    static RandomSource create(long seed) {
        return new RandomSourceImpl(seed);
    }

    static RandomSource next(Random random) {
        return new RandomSourceImpl(random.nextLong());
    }
}
