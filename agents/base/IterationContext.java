package grakn.simulation.agents.base;

import grakn.client.GraknClient.Session;
import grakn.simulation.agents.World;

import java.time.LocalDateTime;

public interface IterationContext {
    Session getIterationGraknSessionFor(String sessionKey);
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
    boolean shouldTrace();
}
