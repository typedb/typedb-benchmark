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

package grakn.benchmark.simulation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSource {

    private final long seed;
    private Random random;

    public RandomSource(long seed) {
        this.seed = seed;
    }

    public static RandomSource nextSource(Random random) {
        return new RandomSource(random.nextLong());
    }

    public Random get() {
        if (random == null) random = new Random(seed);
        return random;
    }

    public List<RandomSource> split(int times) {
        Random random = get();
        List<RandomSource> sources = new ArrayList<>(times);
        for (int i = 0; i < times; ++i) sources.add(nextSource(random));
        return sources;
    }
}
