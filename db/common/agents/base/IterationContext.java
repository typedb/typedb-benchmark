package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.common.driver.DriverWrapper;

import java.time.LocalDateTime;

public interface IterationContext {
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
    boolean shouldTrace();
    ResultHandler getResultHandler();
}
