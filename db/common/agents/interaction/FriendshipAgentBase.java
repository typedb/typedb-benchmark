package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static java.util.Collections.shuffle;

public interface FriendshipAgentBase extends InteractionAgent<World.City> {

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        agent.startAction();
        List<String> residentEmails;
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getResidentEmails"))) {
            residentEmails = getResidentEmails(city, iterationContext.today());
        }
        agent.stopAction();  // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585
        agent.startAction();
        if (residentEmails.size() > 0) {
            shuffle(residentEmails, agent.random());
            int numFriendships = iterationContext.world().getScaleFactor();
            for (int i = 0; i < numFriendships; i++) {

                String friend1 = agent.pickOne(residentEmails);
                String friend2 = agent.pickOne(residentEmails);
                try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertFriendship"))) {
                    insertFriendship(iterationContext.today(), friend1, friend2);
                }
            }
            agent.commitAction();
        }
        return null;
    }

    List<String> getResidentEmails(World.City city, LocalDateTime earliestDate);

    void insertFriendship(LocalDateTime today, String friend1Email, String friend2Email);

}
