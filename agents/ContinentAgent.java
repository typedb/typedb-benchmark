package grakn.simulation.agents;

import grakn.client.GraknClient.Session;
import grakn.simulation.agents.entities.Continent;

import java.util.List;

public interface ContinentAgent extends ParallelAgent<Continent> {

    @Override
    default List<Continent> getParallelItems(Session session) {
        return Continent.getAllSorted(session);
    }
}
