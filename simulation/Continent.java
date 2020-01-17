package grakn.simulation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Continent {
    private final String name;

    public static final List<Continent> ALL = Stream.of(
            "North America",
            "South America",
            "Europe",
            "Asia",
            "Africa",
            "Australia",
            "Antarctica"
    ).map(Continent::new).collect(Collectors.toList());

    private Continent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
