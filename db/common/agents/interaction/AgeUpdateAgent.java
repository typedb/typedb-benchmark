package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CityAgent;
import grakn.simulation.db.common.context.DatabaseContext;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class AgeUpdateAgent<CONTEXT extends DatabaseContext> extends CityAgent<CONTEXT> {

    @Override
    public final AgentResultSet iterate() {
        openTx();
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("updateAgesOfAllPeople"))) {
            updateAgesOfAllPeople();
        }
        commitTx();
        return new AgentResultSet();
    }

    protected abstract void updateAgesOfAllPeople();

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(world().getScaleFactor(), world().getScaleFactor());
    }
}
