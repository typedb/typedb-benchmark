package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DatabaseContext;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class FriendshipAgent<CONTEXT extends DatabaseContext> extends CityAgent<CONTEXT> {

    @Override
    public final AgentResultSet iterate() {
        startAction();
        List<String> residentEmails;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getResidentEmails"))) {
            residentEmails = getResidentEmails(today());
        }
        stopAction();  // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585
        startAction();
        if (residentEmails.size() > 0) {
            shuffle(residentEmails);
            int numFriendships = world().getScaleFactor();
            for (int i = 0; i < numFriendships; i++) {

                String friend1 = pickOne(residentEmails);
                String friend2 = pickOne(residentEmails);
                try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertFriendship"))) {
                    insertFriendship(friend1, friend2);
                }
            }
            commitAction();
        }
        return null;
    }

    protected abstract List<String> getResidentEmails(LocalDateTime earliestDate);

    protected abstract void insertFriendship(String friend1Email, String friend2Email);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(0, world().getScaleFactor());
    }
}
