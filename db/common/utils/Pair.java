package grakn.simulation.db.common.utils;

import java.util.ArrayList;
import java.util.List;

public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public static <T, U> List<Pair<T, U>> zip(List<T> firsts, List<U> seconds) {
        int size = firsts.size();
        if (size != seconds.size()) {
            throw new IllegalArgumentException("Cannot zip lists of different sizes: " + size + ", " + seconds.size());
        }

        List<Pair<T, U>> results = new ArrayList<>();

        for (int i = 0; i < size; ++i) {
            results.add(new Pair<>(firsts.get(i), seconds.get(i)));
        }

        return results;
    }
}
