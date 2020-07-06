package grakn.simulation.config;

import java.util.List;

public class Config {
    private List<Agent> agents;
    private TraceSampling traceSampling;

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public TraceSampling getTraceSampling() {
        return traceSampling;
    }

    public void setTraceSampling(TraceSampling traceSampling) {
        this.traceSampling = traceSampling;
    }

    public static class TraceSampling {
        private String functionName;
        private Integer arg;

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }

        public Integer getArg() {
            return arg;
        }

        public void setArg(Integer arg) {
            this.arg = arg;
        }
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
