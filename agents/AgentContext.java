package grakn.simulation.agents;

import grakn.client.GraknClient.Session;

import java.time.LocalDateTime;

public interface AgentContext {
    Session getIterationGraknSessionFor(String sessionKey);
    int getSimulationStep();
    LocalDateTime getLocalDateTime();
    World getWorld();
}
