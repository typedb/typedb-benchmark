package grakn.benchmark.common.utils;

import grakn.common.collection.Pair;

import java.util.ArrayList;
import java.util.List;

import static grakn.common.collection.Collections.pair;

public class Utils {
    public static <T, U> List<Pair<T, U>> zip(List<T> firsts, List<U> seconds) {
        int size = firsts.size();
        if (size != seconds.size()) {
            throw new IllegalArgumentException("Cannot zip lists of different sizes: " + size + ", " + seconds.size());
        }

        List<Pair<T, U>> results = new ArrayList<>();

        for (int i = 0; i < size; ++i) {
            results.add(pair(firsts.get(i), seconds.get(i)));
        }

        return results;
    }
}
