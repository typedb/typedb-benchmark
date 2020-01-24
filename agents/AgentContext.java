package grakn.simulation.agents;

import grakn.client.GraknClient.Session;

import java.time.LocalDate;

public interface AgentContext {
    Session getGraknSession();
    Session getIterationGraknSessionFor(String sessionKey);
    LocalDate getDate();
    World getWorld();
}
