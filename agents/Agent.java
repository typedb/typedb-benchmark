package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.simulation.common.RandomSource;

/**
 * An agent that will execute some logic across the
 */
public interface Agent {

    void iterate(GraknClient.Session session, RandomSource randomSource);
}
