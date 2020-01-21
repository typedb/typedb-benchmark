package grakn.simulation.agents;

import grakn.client.GraknClient.Session;
import grakn.simulation.common.RandomSource;
import grakn.simulation.agents.entities.Continent;

public class SampleAgent implements ContinentAgent {

    @Override
    public void iterate(Session session, RandomSource randomSource, Continent continent) {
        System.out.println(
                Thread.currentThread().getName() + ": " +
                continent.getContinentName() + ": " +
                randomSource.startNewRandom().nextInt(100)
        );
    }
}
