package grakn.simulation;

public class Main {

    public static void main(String[] args) {

        Agent testAgent = new ContinentAgent() {
            @Override
            void step(SimulationContext context, DeterministicRandomizer randomizer, Continent item) {
                int num = randomizer.createRandom().nextInt(10);
                context.graknClient()
                        .query(Thread.currentThread().getName() + "\t" + item.getName() + "\t" + num);
            }
        };

        DeterministicRandomizer randomizer = new DeterministicRandomizer(1);
        FakeGraknClient client = new FakeGraknClient();

        SimulationContext simulationContext = new SimulationContext() {
            @Override
            public FakeGraknClient graknClient() {
                return client;
            }
        };

        Simulator.simulator()
                .addAgent(testAgent)
                .build()
                .newSimulation(randomizer, simulationContext)
                .run(3);
    }
}
