package grakn.simulation;

/**
 * An agent that will execute some logic across the
 */
public interface Agent {

    void step(SimulationContext simulationContext, DeterministicRandomizer randomizer);
}
