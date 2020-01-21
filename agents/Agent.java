package grakn.simulation.agents;

import grakn.simulation.common.RandomSource;

/**
 * An agent that will execute some logic across the
 */
public interface Agent {

    void iterate(AgentContext context, RandomSource randomSource);
}
