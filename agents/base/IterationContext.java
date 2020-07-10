package grakn.simulation.agents.base;

import grakn.simulation.world.World;
import grakn.simulation.driver.DbDriverWrapper;

import java.time.LocalDateTime;

public interface IterationContext {
    DbDriverWrapper.Session getIterationSessionFor(String sessionKey);
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
    boolean shouldTrace();
}
