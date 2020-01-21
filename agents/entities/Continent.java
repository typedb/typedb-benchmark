package grakn.simulation.agents.entities;

import grakn.client.GraknClient.Session;
import grakn.simulation.common.GraknUtil;

import java.util.List;
import java.util.stream.Collectors;

public class Continent {
    private String continentName;

    public Continent(String continentName) {
        this.continentName = continentName;
    }

    public String getContinentName() {
        return continentName;
    }

    public static List<Continent> getAllSorted(Session session) {
        return GraknUtil.getSortedAttributeValues(session, "continent-name").stream()
                .map(Continent::new)
                .collect(Collectors.toList());
    }
}
