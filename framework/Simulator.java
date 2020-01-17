package grakn.simulation.framework;

import grakn.simulation.framework.agents.Agent;
import grakn.simulation.framework.context.SimulationContext;
import grakn.simulation.framework.random.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulator {

    private final List<Agent> agents;

    private Simulator(List<Agent> agents) {
        this.agents = Collections.unmodifiableList(agents);
    }

    public Simulation newSimulation(SimulationContext context) {
        return new Simulation(this, context);
    }

    List<Agent> getAgents() {
        return agents;
    }

    public static Builder simulator() {
        return new Builder();
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
