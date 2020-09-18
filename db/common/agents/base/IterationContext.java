package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

public interface IterationContext {
    int simulationStep();
    LocalDateTime today();
    World world();
    boolean shouldTrace();
    ResultHandler getResultHandler();
}
