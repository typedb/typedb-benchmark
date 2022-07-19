/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.benchmark.common.seed;

import com.vaticle.typedb.benchmark.common.concept.City;
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.common.collection.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.vaticle.typedb.common.collection.Collections.pair;

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

    public <T> T choose(List<T> list) {
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

    public <T> List<Pair<T, T>> randomPairs(List<T> list, int pairsPerElement) {
        List<Pair<T, T>> pairs = new ArrayList<>(list.size() * pairsPerElement);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < pairsPerElement; j++) {
                int other = random.nextInt(list.size() - 1);
                if (other >= i) other++;
                pairs.add(pair(list.get(i), list.get(other)));
            }
        }
        return pairs;
    }

    public <T> List<Pair<T, T>> randomPairs(List<T> list1, List<T> list2) {
        int numPairs = Math.min(list1.size(), list2.size());
        List<Pair<T, T>> pairs = new ArrayList<>(numPairs);
        Collections.shuffle(list2, random);
        for (int i = 0; i < numPairs; i++) {
            pairs.add(new Pair<>(list1.get(i), list2.get(i)));
        }
        return pairs;
    }

    public <RECIPIENT, RESOURCE> List<Pair<RECIPIENT, RESOURCE>> randomAllocation(List<RECIPIENT> recipients,
                                                                                  List<RESOURCE> resources) {
        List<Pair<RECIPIENT, RESOURCE>> allocations = new ArrayList<>(resources.size());
        if (recipients.size() == 0) {
            return allocations;
        } else if (recipients.size() == 1) {
            resources.forEach(resource -> allocations.add(new Pair<>(recipients.get(0), resource)));
        } else {
            resources.forEach(resource -> allocations.add(
                    new Pair<>(recipients.get(random.nextInt(recipients.size() - 1)), resource)));
        }
        return allocations;
    }
}
