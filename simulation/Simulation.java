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

package grakn.benchmark.simulation;

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.AgentFactory;
import grakn.benchmark.simulation.agent.base.AgentManager;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.utils.RandomSource;
import grakn.benchmark.simulation.world.World;
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

public abstract class Simulation<DB_DRIVER extends Client<TX>, TX extends Transaction> implements grakn.benchmark.simulation.agent.base.BenchmarkContext {

    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private final List<AgentManager<?, TX>> agentMgrs;
    protected final DB_DRIVER driver;
    private final Random random;
    private final List<Config.Agent> agentConfigs;
    private final Function<Integer, Boolean> iterationSamplingFunction;
    private final Report report;
    private final World world;
    private final boolean test;
    private int iteration = 1;

    public Simulation(DB_DRIVER driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        this.driver = driver;
        this.random = new Random(randomSeed);
        this.agentConfigs = agentConfigs;
        this.iterationSamplingFunction = iterationSamplingFunction;
        this.world = world;
        this.test = test;
        initialise(initialisationDataPaths);
        this.agentMgrs = agentListFromConfigs();
        this.report = new Report();
    }

    protected List<AgentManager<?, TX>> agentListFromConfigs() {
        List<AgentManager<?, TX>> agents = new ArrayList<>();
        ActionFactory<TX, ?> actionFactory = actionFactory();
        AgentFactory<TX, ?> agentFactory = new AgentFactory<>(driver, actionFactory, this);

        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                AgentManager<?, TX> agent = agentFactory.get(agentConfig.getName());
                agent.setTracing(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    protected abstract ActionFactory<TX, ?> actionFactory();

    protected abstract void initialise(Map<String, Path> initialisationDataPaths);

    public void iterate() {
        report.clean();
        for (AgentManager<?, ?> agentMgr : agentMgrs) {
            this.report.addAgentResult(agentMgr.name(), agentMgr.iterate(RandomSource.nextSource(random)));
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

    public abstract void printStatistics(Logger LOG);

    public class Report {

        private ConcurrentHashMap<String, AgentManager<?, ?>.Report> agentReports = new ConcurrentHashMap<>();

        public void addAgentResult(String agentName, AgentManager<?, ?>.Report agentReport) {
            if (agentReport == null) {
                throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
            }
            agentReports.put(agentName, agentReport);
        }

        public AgentManager<?, ?>.Report getAgentReport(String agentName) {
            return agentReports.get(agentName);
        }

        public void clean() {
            agentReports = new ConcurrentHashMap<>();
        }
    }
}
