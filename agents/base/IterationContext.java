package grakn.simulation.agents.base;

import grakn.simulation.world.World;
import grakn.simulation.driver.DriverWrapper;

import java.time.LocalDateTime;

public interface IterationContext {
    DriverWrapper.Session getIterationSessionFor(String sessionKey);
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
    boolean shouldTrace();
}
