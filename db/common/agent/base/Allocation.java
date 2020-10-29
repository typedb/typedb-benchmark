package grakn.simulation.db.common.agent.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Tools to allocate items to some number of buckets
 * Example use-case: for a set of newborns and a set of marriages, evenly allocate the newborns across the marriages
 */
public class Allocation {

    public static List<Integer> allocateEvenly(Integer numThingsToAllocate, Integer numBuckets){
        List<Integer> allocations = new ArrayList<>();
        for (int i = 0; i < numThingsToAllocate; i++) {
            allocations.add(i % numBuckets);
        }
        return allocations;
    }

    public static LinkedHashMap<Integer, List<Integer>> allocateEvenlyToMap(Integer numThingsToAllocate, Integer numBuckets){
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