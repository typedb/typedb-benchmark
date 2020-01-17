package grakn.simulation.world;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import grakn.simulation.framework.agents.Agent;
import grakn.simulation.framework.context.GraknSimulationContext;
import grakn.simulation.framework.context.SimulationContext;
import grakn.simulation.framework.random.RandomSource;
import grakn.simulation.framework.Simulator;
import grakn.simulation.world.agents.SampleAgent;

public class Main {

    private static final Agent[] AGENTS = {
            new SampleAgent()
    };

    public static void main(String[] args) {

        GraknClient client = new GraknClient();

        Session session = client.session("world");
        RandomSource randomSource = RandomSource.create(1);

        SimulationContext context = new GraknSimulationContext(session, randomSource);

        Simulator.Builder simulatorBuilder = Simulator.simulator();
        for (Agent agent : AGENTS) {
            simulatorBuilder.addAgent(agent);
        }

        simulatorBuilder.build()
                .newSimulation(context)
                .run(3);
    }
}
