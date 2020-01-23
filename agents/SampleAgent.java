package grakn.simulation.agents;

import grakn.simulation.common.RandomSource;

public class SampleAgent implements ContinentAgent {

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.Continent continent) {
        System.out.println(
                Thread.currentThread().getName() + ": " +
                continent.getName() + ": " +
                context.getLocalDateTime() + ": " +
                randomSource.startNewRandom().nextInt(100)
        );
    }
}
