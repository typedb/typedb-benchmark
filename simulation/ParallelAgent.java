package grakn.simulation;

import java.util.List;

public abstract class ParallelAgent<T> implements Agent {

    abstract void step(SimulationContext context, DeterministicRandomizer randomizer, T item);

    abstract List<T> getParallelList();

    public void step(SimulationContext context, DeterministicRandomizer randomizer) {

        // TODO manually control threads
        randomizer.split(getParallelList()).parallelStream()
                .forEach(e ->  step(context, e.getRandomizer(), e.getItem()));
    }
}
