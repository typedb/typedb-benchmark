package grakn.simulation.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Allocation {

    public static List<Integer> allocateEvenly(Integer numThingsToAllocate, Integer numBuckets){
        List<Integer> allocations = new ArrayList<>();
        for (int i = 0; i < numThingsToAllocate; i++) {
            allocations.add(i % numBuckets);
        }
        return allocations;
    }

    public static HashMap<Integer, List<Integer>> allocateEvenlyToHashMap(Integer numThingsToAllocate, Integer numBuckets){
        HashMap<Integer, List<Integer>> allocationMap = new HashMap<>();
        List<Integer> allocations = allocateEvenly(numThingsToAllocate, numBuckets);
        for (int i = 0; i < allocations.size(); i++) {
            Integer allocation = allocations.get(i);
            List<Integer> list = allocationMap.computeIfAbsent(allocation, a -> new ArrayList<>());
            list.add(i);
        }
        return allocationMap;
    }
}
