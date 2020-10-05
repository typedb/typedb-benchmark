package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface AgeUpdateAgentBase extends Agent.RegionalAgent<World.City> {

    @Override
    default void iterate(DbOperationController<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        agent.startDbOperation("updateAgesOfAllPeople", tracker);
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            updateAgesOfAllPeople(simulationContext.today(), city);
        }
        agent.saveDbOperation();
        return new ActionResultList();
    }

    void updateAgesOfAllPeople(LocalDateTime today, World.City city);
}
