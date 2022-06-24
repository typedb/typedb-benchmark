/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.benchmark.common.params;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

public class Config {
    private List<Agent> agents;
    private TraceSampling traceSampling;

    private static final RunParams DEFAULT_RUN_PARAMS = new RunParams();
    private static final ModelParams DEFAULT_MODEL_PARAMS = new ModelParams();

    private RunParams runParams = DEFAULT_RUN_PARAMS;
    private ModelParams modelParams = DEFAULT_MODEL_PARAMS;

    public static Config loadYML(File file) throws YAMLException {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        try {
            InputStream inputStream = new FileInputStream(new File(file.toPath().toString()));
            return yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't find config file");
        }
    }

    public List<Agent> agents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public void setRunParams(RunParams runParams) {
        this.runParams = runParams;
    }

    public RunParams runParams() {
        return runParams;
    }

    public void setModelParams(ModelParams modelParams) {
        this.modelParams = modelParams;
    }

    public ModelParams modelParams() {
        return modelParams;
    }

    public TraceSampling traceSampling() {
        return traceSampling;
    }

    public void setTraceSampling(TraceSampling traceSampling) {
        this.traceSampling = traceSampling;
    }

    public static class RunParams {
        private final static int DEFAULT_RANDOM_SEED = 1;
        private final static int DEFAULT_NUM_ITERATIONS = 10;
        private final static int DEFAULT_SCALE_FACTOR = 5;
        private final static String DEFAULT_DATABASE_NAME = "world";

        private int randomSeed = DEFAULT_RANDOM_SEED;
        private int iterations = DEFAULT_NUM_ITERATIONS;
        private int scaleFactor = DEFAULT_SCALE_FACTOR;
        private String databaseName = DEFAULT_DATABASE_NAME;

        public int randomSeed() {
            return randomSeed;
        }

        public void setRandomSeed(int randomSeed) {
            this.randomSeed = randomSeed;
        }

        public int iterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public int scaleFactor() {
            return scaleFactor;
        }

        public void setScaleFactor(int scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        public String databaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
    }

    public static class ModelParams {

        private final static int DEFAULT_AGE_OF_FRIENDSHIP = 14;
        private final static int DEFAULT_AGE_OF_ADULTHOOD = 21;
        private final static int DEFAULT_YEARS_BEFORE_PARENTHOOD = 3;

        private int ageOfFriendship = DEFAULT_AGE_OF_FRIENDSHIP;
        private int ageOfAdulthood = DEFAULT_AGE_OF_ADULTHOOD;
        private int yearsBeforeParenthood = DEFAULT_YEARS_BEFORE_PARENTHOOD;

        public int ageOfFriendship() {
            return ageOfFriendship;
        }

        public void setAgeOfFriendship(int ageOfFriendship) {
            this.ageOfFriendship = ageOfFriendship;
        }

        public int ageOfAdulthood() {
            return ageOfAdulthood;
        }

        public void setAgeOfAdulthood(int ageOfAdulthood) {
            this.ageOfAdulthood = ageOfAdulthood;
        }

        public int yearsBeforeParenthood() {
            return yearsBeforeParenthood;
        }

        public void setYearsBeforeParenthood(int yearsBeforeParenthood) {
            this.yearsBeforeParenthood = yearsBeforeParenthood;
        }
    }

    public static class TraceSampling {
        private SamplingFunction function;
        private Integer arg;

        public Function<Integer, Boolean> samplingFunction() {
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

        public enum SamplingFunction {

            EVERY(new int[0]),
            LOG(new int[0]);

            private final int[] acceptedArgs;

            SamplingFunction(int[] acceptedArgs) {
                this.acceptedArgs = acceptedArgs;
            }

            public static SamplingFunction getByName(String name) {
                for (SamplingFunction samplingFunction : SamplingFunction.values()) {
                    if (samplingFunction.name().toLowerCase().equals(name.toLowerCase())) {
                        return samplingFunction;
                    }
                }
                throw new IllegalArgumentException("Function name not recognised");
            }

            public int[] acceptedArgs() {
                return acceptedArgs;
            }

            public static Function<Integer, Boolean> applyArg(SamplingFunction sampling, Integer arg) {
                switch (sampling) {
                    case EVERY:
                        if (arg < 1) throw new IllegalArgumentException("`every` requires an argument of 1 or greater");
                        return i -> i % arg == 0;
                    case LOG:
                        if (arg < 2) throw new IllegalArgumentException("`log` requires a base of 2 or greater");
                        // return true if the logarithm of `i` to the given base is an integer
                        return i -> ((int) Math.log(i) / Math.log(arg) % 1) == 0;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
    }

    public static class Agent {
        // TODO: mode should just be replaced with two booleans: 'run' and 'trace'
        private Mode mode;
        private String name;

        public enum Mode {TRACE, RUN, OFF}

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public boolean isDisabled() {
            return mode == Mode.OFF;
        }

        public boolean isRun() {
            return mode == Mode.RUN || mode == Mode.TRACE;
        }

        public boolean isTracing() {
            return mode == Mode.TRACE;
        }
    }
}
