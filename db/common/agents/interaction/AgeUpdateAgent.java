package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class AgeUpdateAgent extends CityAgent {

    @Override
    public final void iterate() {
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("updateAgesOfAllPeople"))) {
            updateAgesOfAllPeople();
        }
        tx().commitWithTracing();
    }

    protected abstract void updateAgesOfAllPeople();
}
