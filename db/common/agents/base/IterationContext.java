package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.common.driver.DriverWrapper;

import java.time.LocalDateTime;

public interface IterationContext<Session extends DriverWrapper.Session> {
    Session getIterationSessionFor(String sessionKey);
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
    boolean shouldTrace();
}
