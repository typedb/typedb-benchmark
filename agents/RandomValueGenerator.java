package grakn.simulation.agents;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * Helpers for generating values based on a given random generator
 */
class RandomValueGenerator {
    private final Random random;

    RandomValueGenerator(Random random) {
        this.random = random;
    }

    private static double doubleInterpolate(double in, double min, double max) {
        return (in * (max - min)) + min;
    }

    static int boundRandomInt(Random random, int min, int max) {
        return random.nextInt(max - min) + min;
    }

    String boundRandomLengthRandomString(int minLength, int maxLength) {
        return RandomStringUtils.random(boundRandomInt(random, minLength, maxLength), 0, 0, true, true, null, random);
    }

    double boundRandomDouble(Double min, Double max) {
        return doubleInterpolate(random.nextDouble(), min, max);
    }
}
