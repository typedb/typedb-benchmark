package grakn.simulation.agents;

import grakn.client.GraknClient.Session;

import java.time.LocalDate;

public interface AgentContext {
    Session getGraknSession();
    LocalDate getDate();
}
