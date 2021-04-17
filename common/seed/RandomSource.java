/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.common.seed;

import grakn.benchmark.common.concept.City;
import grakn.benchmark.common.concept.Gender;

import java.util.ArrayList;
import java.util.Random;

public class RandomSource {

    private final Random random;

    public RandomSource(long seed) {
        this.random = new Random(seed);
    }

    public RandomSource nextSource() {
        return new RandomSource(random.nextLong());
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public <T> T choose(ArrayList<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public int nextInt() {
        return random.nextInt(Integer.MAX_VALUE);
    }

    public String address(City city) {
        return String.format("%s %s Street, %s, %s %s", random.nextInt(1000),
                             choose(city.country().continent().commonFirstNames(Gender.of(nextBoolean()))),
                             city.name(), random.nextInt(10_000), city.country().name());
    }
}
