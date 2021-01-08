/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.config;

import java.util.List;
import java.util.function.Function;

public class Config {
    private final static int DEFAULT_RANDOM_SEED = 1;
    private final static int DEFAULT_NUM_ITERATIONS = 10;
    private final static int DEFAULT_SCALE_FACTOR = 5;
    private final static String DEFAULT_DATABASE_NAME = "world";

    private List<Agent> agents;
    private TraceSampling traceSampling;
    private int randomSeed = DEFAULT_RANDOM_SEED;
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

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int randomSeed) {
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
        private SamplingFunction function;
        private Integer arg;

        public Function<Integer, Boolean> getSamplingFunction() {
            return SamplingFunction.applyArg(function, arg);
        }

        public void setFunction(String function) {
            this.function = SamplingFunction.getByName(function);
        }

        public Integer getArg() {
            return arg;
        }

        public void setArg(Integer arg) {
            this.arg = arg;
        }
    }

    public static class Agent {
        private AgentMode agentMode;
        private String name;

        public static Agent ConstructAgentConfig(String name, AgentMode agentMode) {
            // This method is needed because snakeyaml doesn't support declaring a constructor for this class
            Agent agent = new Agent();
            agent.setName(name);
            agent.setMode(agentMode);
            return agent;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setMode(AgentMode mode) {
            this.agentMode = mode;
        }

        public AgentMode getAgentMode() {
            return agentMode;
        }
    }
}
