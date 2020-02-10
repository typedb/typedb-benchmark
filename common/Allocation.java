package grakn.simulation.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
}
