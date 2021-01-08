/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.common.agent.base;

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
        return (double) Math.round(value * 100000d) / 100000d; // Not 100% accurate, but serves our purpose to ensure different backends are given a precision they can handle
    }

    public boolean bool() {
        return random.nextBoolean();
    }
}
