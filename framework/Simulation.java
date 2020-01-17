package grakn.simulation.framework;

import grakn.simulation.framework.context.SimulationContext;
import grakn.simulation.framework.random.RandomSource;

import java.util.Random;

public class Simulation {
    private Simulator simulator;
    private int simulationStep = 0;
    private Random random;
    private SimulationContext context;

    Simulation(Simulator simulator, SimulationContext context) {
        this.simulator = simulator;
        random = context.getRandomSource().start();
        this.context = context;
    }

    public void step() {
        System.out.println("**************************");
        System.out.println("Simulation step: " + simulationStep);
        System.out.println("**************************");

        RandomSource randomSource = RandomSource.next(random);

        randomSource.split(simulator.getAgents()).forEach(e -> e.getItem().step(context));

        simulationStep++;
    }

    public void run(int steps) {
        for (int i = 0; i < steps; ++i) {
            step();
        }
    }

    public void runUntil(int step) {
        while (simulationStep < step) {
            step();
        }
    }

    public int getCurrentStep() {
        return simulationStep;
    }
}
