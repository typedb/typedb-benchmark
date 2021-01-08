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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Tools to allocate items to some number of buckets
 * Example use-case: for a set of newborns and a set of marriages, evenly allocate the newborns across the marriages
 */
public class Allocation {

    public static List<Integer> allocateEvenly(Integer numThingsToAllocate, Integer numBuckets) {
        List<Integer> allocations = new ArrayList<>();
        for (int i = 0; i < numThingsToAllocate; i++) {
            allocations.add(i % numBuckets);
        }
        return allocations;
    }

    public static LinkedHashMap<Integer, List<Integer>> allocateEvenlyToMap(Integer numThingsToAllocate, Integer numBuckets) {
        LinkedHashMap<Integer, List<Integer>> allocationMap = new LinkedHashMap<>();
        List<Integer> allocations = allocateEvenly(numThingsToAllocate, numBuckets);
        for (int i = 0; i < allocations.size(); i++) {
            Integer allocation = allocations.get(i);
            List<Integer> list = allocationMap.computeIfAbsent(allocation, a -> new ArrayList<>());
            list.add(i);
        }
        return allocationMap;
    }

    public static <T, U> boolean allocate(List<T> toAllocate, List<U> buckets, BiConsumer<T, U> insertFunction) {
        if (toAllocate.size() > 0 && buckets.size() > 0) {

            List<Integer> allocations = Allocation.allocateEvenly(toAllocate.size(), buckets.size());

            for (int i = 0; i < allocations.size(); i++) {
                T item = toAllocate.get(i);
                U bucket = buckets.get(allocations.get(i));
                insertFunction.accept(item, bucket);
            }
            return true;
        }
        return false;
    }
}