package grakn.simulation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSource {
    private final long seed;

    public RandomSource(long seed) {
        this.seed = seed;
    }

    public static RandomSource nextSource(Random random) {
        return new RandomSource(random.nextLong());
    }

    public Random startNewRandom() {
        return new Random(seed);
    }

    public List<RandomSource> split(int times) {
        Random random = startNewRandom();
        List<RandomSource> sources = new ArrayList<>(times);
        for (int i = 0; i < times; ++i) {
            sources.add(nextSource(random));
        }
        return sources;
    }
}
