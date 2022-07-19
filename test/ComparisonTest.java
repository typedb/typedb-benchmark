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

package com.vaticle.typedb.benchmark.test;

import com.vaticle.typedb.benchmark.common.params.Config;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.typedb.TypeDBSimulation;
import com.vaticle.typedb.benchmark.neo4j.Neo4JSimulation;
import com.vaticle.typedb.benchmark.simulation.Simulation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.vaticle.typedb.benchmark.common.params.Options.parseCLIOptions;
import static com.vaticle.typedb.benchmark.test.ComparisonTest.Suite.TYPEDB;
import static com.vaticle.typedb.benchmark.test.ComparisonTest.Suite.NEO4J;
import static org.junit.Assert.assertEquals;

@RunWith(ComparisonTest.Suite.class)
public class ComparisonTest {

    @Test
    public void test_agents_have_equal_reports() {
        Simulation.REGISTERED_AGENTS.forEach(agent -> assertEquals(TYPEDB.getReport(agent), NEO4J.getReport(agent)));
    }

    public static class Suite extends org.junit.runners.Suite {

        private static final Config CONFIG = Config.loadYML(Paths.get("test/comparison-test.yml").toFile());
        private static final Options OPTIONS = parseCLIOptions(args(), new Options()).get();

        public static TypeDBSimulation TYPEDB;

        public static Neo4JSimulation NEO4J;
        private int iteration = 1;

        public Suite(Class<?> testClass) throws Throwable {
            super(testClass, createRunners(testClass));
            TYPEDB = TypeDBSimulation.core(OPTIONS.typeDBAddress(), Context.create(CONFIG, false, true));
            NEO4J = Neo4JSimulation.create(OPTIONS.neo4jAddress(), Context.create(CONFIG, false, true));
        }

        private static String[] args() {
            String[] input = System.getProperty("sun.java.command").split(" ");
            return Arrays.copyOfRange(input, 1, input.length);
        }

        private static List<org.junit.runner.Runner> createRunners(Class<?> testClass) throws InitializationError {
            List<org.junit.runner.Runner> runners = new ArrayList<>();
            for (int i = 1; i <= CONFIG.runParams().iterations(); i++) {
                BlockJUnit4ClassRunner runner = new Runner(testClass, i);
                runners.add(runner);
            }
            return runners;
        }

        @Override
        protected void runChild(org.junit.runner.Runner runner, final RunNotifier notifier) {
            iteration++;
            Stream.of(NEO4J, TYPEDB).parallel().forEach(Simulation::iterate);
            super.runChild(runner, notifier);
            if (iteration == CONFIG.runParams().iterations() + 1) {
                TYPEDB.close();
                NEO4J.close();
            }
        }

        private static class Runner extends BlockJUnit4ClassRunner {
            private final int iteration;

            public Runner(Class<?> aClass, int iteration) throws InitializationError {
                super(aClass);
                this.iteration = iteration;
            }

            @Override
            protected String testName(FrameworkMethod method) {
                return method.getName() + "-iter-" + iteration;
            }

            @Override
            protected String getName() {
                return super.getName() + "-iter-" + iteration;
            }
        }

        @CommandLine.Command(name = "benchmark-test", mixinStandardHelpOptions = true)
        private static class Options {

            @CommandLine.Option(names = {"--typedb"}, required = true, description = "Database address URI")
            private String typeDBAddress;

            @CommandLine.Option(names = {"--neo4j"}, required = true, description = "Database address URI")
            private String neo4jAddress;

            public String typeDBAddress() {
                return typeDBAddress;
            }

            public String neo4jAddress() {
                return neo4jAddress;
            }
        }
    }
}
