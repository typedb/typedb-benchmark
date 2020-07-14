package grakn.simulation.agents.utils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tracker {
    public static String of(Object... items) {
        return Stream.of(items).map(Object::toString).collect(Collectors.joining(":"));
    }
}
