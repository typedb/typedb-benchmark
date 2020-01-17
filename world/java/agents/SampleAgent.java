package grakn.simulation.world.agents;

import grakn.simulation.framework.context.SimulationContext;
import grakn.simulation.world.entities.Continent;

public class SampleAgent implements ContinentAgent {
    @Override
    public void step(SimulationContext context, Continent continent) {
        System.out.println(
                Thread.currentThread().getName() + ": " +
                continent.getContinentName() + ": " +
                context.getRandomSource().start().nextInt(100)
        );
    }
}
