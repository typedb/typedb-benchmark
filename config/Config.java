package grakn.simulation.config;

import java.util.List;
import java.util.function.Function;

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
        private Schema.SamplingFunction function;
        private Integer arg;

        public Function<Integer, Boolean> getSamplingFunction() {
            return Schema.SamplingFunction.applyArg(function, arg);
        }

        public void setFunction(String function) {
            this.function = Schema.SamplingFunction.getByName(function);
        }

        public Integer getArg() {
            return arg;
        }

        public void setArg(Integer arg) {
            this.arg = arg;
        }
    }

    public static class Agent {
        private Schema.AgentMode agentMode;
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setMode(String mode) {
            switch (mode) {
                case "trace":
                    this.agentMode = Schema.AgentMode.TRACE;
                    break;
                case "run":
                    this.agentMode = Schema.AgentMode.RUN;
                    break;
                case "off":
                    this.agentMode = Schema.AgentMode.OFF;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unrecognised agent mode %s", mode));
            }
        }

        public Schema.AgentMode getAgentMode() {
            return agentMode;
        }
    }
}
