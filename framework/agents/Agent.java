package grakn.simulation.framework.agents;

import grakn.simulation.framework.context.SimulationContext;

/**
 * An agent that will execute some logic across the
 */
public interface Agent {

    void step(SimulationContext context);
}
