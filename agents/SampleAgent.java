package grakn.simulation.agents;

import grakn.simulation.common.RandomSource;

public class SampleAgent implements ContinentAgent {

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, String continentName) {
        System.out.println(
                Thread.currentThread().getName() + ": " +
                continentName + ": " +
                context.getDate() + ": " +
                randomSource.startNewRandom().nextInt(100)
        );
    }
}
