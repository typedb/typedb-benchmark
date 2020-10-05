package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.Simulation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

public interface SimulationContext {
    int simulationStep();
    LocalDateTime today();
    World world();
    boolean trace();
    boolean test();
    Simulation.Report getReport();
}
