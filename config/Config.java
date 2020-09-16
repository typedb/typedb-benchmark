package grakn.simulation.config;

import java.util.List;
import java.util.function.Function;

public class Config {
    private final static long DEFAULT_RANDOM_SEED = 1;
    private final static int DEFAULT_NUM_ITERATIONS = 10;
    private final static int DEFAULT_SCALE_FACTOR = 5;
    private final static String DEFAULT_DATABASE_NAME = "world";

    private List<Agent> agents;
    private TraceSampling traceSampling;
    private long randomSeed = DEFAULT_RANDOM_SEED;
    private int iterations = DEFAULT_NUM_ITERATIONS;
    private int scaleFactor = DEFAULT_SCALE_FACTOR;
    private String databaseName = DEFAULT_DATABASE_NAME;

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

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

//        public static Agent ConstructAgent(String name, Schema.AgentMode agentMode) {
        public Agent(String name, Schema.AgentMode agentMode) {
            // snakeyaml doesn't support having a second constructor for this class
            this.name = name;
            this.agentMode = agentMode;
        }

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
