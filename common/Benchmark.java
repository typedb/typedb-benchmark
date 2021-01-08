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

package grakn.benchmark.common;

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.agent.AgentFactory;
import grakn.benchmark.common.agent.base.Agent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.utils.RandomSource;
import grakn.benchmark.common.world.World;
import grakn.benchmark.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Benchmark<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> implements grakn.benchmark.common.agent.base.BenchmarkContext {

    final static Logger LOG = LoggerFactory.getLogger(grakn.benchmark.common.Benchmark.class);
    private final List<Agent<?, DB_OPERATION>> agentList;
    protected final DB_DRIVER driver;
    private final Random random;
    private final List<Config.Agent> agentConfigs;
    private final Function<Integer, Boolean> iterationSamplingFunction;
    private final Report report;
    private final World world;
    private final boolean test;
    private int iteration = 1;

    public Benchmark(DB_DRIVER driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        this.driver = driver;
        this.random = new Random(randomSeed);
        this.agentConfigs = agentConfigs;
        this.iterationSamplingFunction = iterationSamplingFunction;
        this.world = world;
        this.test = test;
        initialise(initialisationDataPaths);
        this.agentList = agentListFromConfigs();
        this.report = new Report();
    }

    protected List<Agent<?, DB_OPERATION>> agentListFromConfigs() {
        List<Agent<?, DB_OPERATION>> agents = new ArrayList<>();
        ActionFactory<DB_OPERATION, ?> actionFactory = actionFactory();
        AgentFactory<DB_OPERATION, ?> agentFactory = new AgentFactory<>(driver, actionFactory, this);

        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                Agent<?, DB_OPERATION> agent = agentFactory.get(agentConfig.getName());
                agent.setTrace(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    protected abstract ActionFactory<DB_OPERATION, ?> actionFactory();

    protected abstract void initialise(Map<String, Path> initialisationDataPaths);

    public void iterate() {

        LOG.info("Iteration: {}", iteration);
        report.clean();
        for (Agent<?, ?> agent : agentList) {
            this.report.addAgentResult(agent.name(), agent.iterate(RandomSource.nextSource(random)));
        }
        closeIteration();  // We want to test opening new sessions each iteration.
        iteration++;
    }

    protected abstract void closeIteration();

    @Override
    public int iteration() {
        return iteration;
    }

    @Override
    public LocalDateTime today() {
        return LocalDateTime.of(LocalDate.ofYearDay(iteration, 1), LocalTime.of(0, 0, 0));
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public boolean trace() {
        return iterationSamplingFunction.apply(iteration());
    }

    @Override
    public boolean test() {
        return test;
    }

    public Report getReport() {
        return report;
    }

    public abstract void close();

    public class Report {

        private ConcurrentHashMap<String, Agent<?, ?>.Report> agentReports = new ConcurrentHashMap<>();

        public void addAgentResult(String agentName, Agent<?, ?>.Report agentReport) {
            if (agentReport == null) {
                throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
            }
            agentReports.put(agentName, agentReport);
        }

        public Agent<?, ?>.Report getAgentReport(String agentName) {
            return agentReports.get(agentName);
        }

        public void clean() {
            agentReports = new ConcurrentHashMap<>();
        }
    }
}
