package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.Region;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface InteractionAgent<REGION extends Region> extends AutoCloseable {

    default String name() {
        return this.getClass().getSimpleName();
    }

    void iterate(Agent<?> agent, REGION region, SimulationContext simulationContext);

    default void iterateWithTracing(Agent<?> agent, REGION region, SimulationContext simulationContext) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(name())) {
            System.out.println(name());
            iterate(agent, region, simulationContext);
        }
    }
}
