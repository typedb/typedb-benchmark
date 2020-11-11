package grakn.simulation.common.agent.base;

import grakn.simulation.common.world.World;

import java.time.LocalDateTime;

public interface SimulationContext {
    int simulationStep();
    LocalDateTime today();
    World world();
    boolean trace();
    boolean test();
}
