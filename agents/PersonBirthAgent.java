package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.simulation.common.RandomSource;

public class PersonBirthAgent implements CityAgent {
    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        GraknClient.Session session = context.getIterationGraknSessionFor(city.getCountry().getContinent().getName());

        session.transaction().write();
    }
}
