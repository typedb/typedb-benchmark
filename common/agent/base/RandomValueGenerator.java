package grakn.simulation.common.agent.base;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * Helpers for generating values based on a given random generator
 */
public class RandomValueGenerator {
    private final Random random;

    public RandomValueGenerator(Random random) {
        this.random = random;
    }

    private static double doubleInterpolate(double in, double min, double max) {
        return (in * (max - min)) + min;
    }

    public int boundRandomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public String boundRandomLengthRandomString(int minLength, int maxLength) {
        return RandomStringUtils.random(boundRandomInt(minLength, maxLength), 0, 0, true, true, null, random);
    }

    public double boundRandomDouble(Double min, Double max) {
        double value = doubleInterpolate(random.nextDouble(), min, max);
        return (double)Math.round(value * 100000d) / 100000d; // Not 100% accurate, but serves our purpose to ensure different backends are given a precision they can handle
    }

    public boolean bool() {
        return random.nextBoolean();
    }
}
