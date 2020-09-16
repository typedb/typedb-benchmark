package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CityAgent;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class AgeUpdateAgent extends CityAgent {

    @Override
    public final AgentResultSet iterate() {
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("updateAgesOfAllPeople"))) {
            updateAgesOfAllPeople();
        }
        commitTxWithTracing();
        return new AgentResultSet();
    }

    protected abstract void updateAgesOfAllPeople();

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(world().getScaleFactor(), world().getScaleFactor());
    }
}
