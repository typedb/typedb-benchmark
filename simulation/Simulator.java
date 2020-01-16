package grakn.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulator {

    private final List<Agent> agents;

    private Simulator(List<Agent> agents) {
        this.agents = agents;
    }

    public Simulation newSimulation(DeterministicRandomizer randomizer, SimulationContext context) {
        return new Simulation(randomizer, context);
    }

    public static Builder simulator() {
        return new Builder();
    }

    public class Simulation {
        private int simulationStep = 0;
        private Random random;
        private SimulationContext context;

        private Simulation(DeterministicRandomizer randomizer, SimulationContext context) {
            random = randomizer.createRandom();
            this.context = context;
        }

        public int step() {
            System.out.println("**************************");
            System.out.println("Simulation step: " + simulationStep);
            System.out.println("**************************");

            DeterministicRandomizer randomizer = new DeterministicRandomizer(random.nextLong());

            randomizer.split(agents).forEach(e -> {
                e.getItem().step(context, e.getRandomizer());
            });

            return ++simulationStep;
        }

        public int run(int steps) {
            int last = simulationStep;
            for (int i = 0; i < steps; ++i) {
                last = step();
            }
            return simulationStep;
        }
    }

    public static class Builder {
        private boolean built = false;
        private final List<Agent> agents = new ArrayList<>();

        public Builder addAgent(Agent agent) {
            if (built) {
                throw new IllegalStateException("Builder completed.");
            }
            agents.add(agent);
            return this;
        }

        public Simulator build() {
            built = true;
            return new Simulator(agents);
        }
    }
}
