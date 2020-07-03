package grakn.simulation.config;

import java.util.List;

public class Config {
    private List<Agent> agents;
    private String iterationTraceSampling;

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public String getIterationTraceSampling() {
        return iterationTraceSampling;
    }

    public void setIterationTraceSampling(String iterationTraceSampling) {
        this.iterationTraceSampling = iterationTraceSampling;
    }

    public static class Agent {
        private String name;
        private Boolean run;
        private Boolean trace;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getTrace() {
            return trace;
        }

        public Boolean getRun() {
            return run;
        }

        public void setMode(String mode) {
            if (mode.equals("trace")) {
                run = true;
                trace = true;
            } else if (mode.equals("run")) {
                run = true;
                trace = false;
            } else {
                run = false;
                trace = false;
            }
        }
    }
}
