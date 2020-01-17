package grakn.simulation.framework.agents;

import grakn.simulation.framework.context.SimulationContext;

import java.util.List;

public interface ParallelAgent<T> extends Agent {

    void step(SimulationContext context, T item);

    List<T> getParallelList(SimulationContext context);

    @Override
    default void step(SimulationContext context) {

        // TODO manually control threads
        context.split(getParallelList(context)).parallelStream()
                .forEach(e -> step(e.getContext(), e.getItem()));
    }
}
