package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface AgeUpdateAgentBase extends InteractionAgent<World.City> {

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        agent.newAction("updateAgesOfAllPeople");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            updateAgesOfAllPeople(iterationContext.today(), city);
        }
        agent.commitAction();
        return new AgentResultSet();
    }

    void updateAgesOfAllPeople(LocalDateTime today, World.City city);
}
