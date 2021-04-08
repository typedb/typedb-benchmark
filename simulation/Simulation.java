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

import grakn.benchmark.config.Config;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.agent.AgentFactory;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.common.RandomSource;
import grakn.benchmark.simulation.common.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Simulation<CLIENT extends Client<?, TX>, TX extends Transaction> implements SimulationContext {

    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private final List<Agent<?, TX>> agents;
    protected final CLIENT client;
    private final RandomSource randomSource;
    private final List<Config.Agent> agentConfigs;
    private final Function<Integer, Boolean> iterationSamplingFunction;
    private final World world;
    private final Map<Class<? extends Agent>, Map<String, List<Action<?, ?>.Report>>> agentReports;
    private final boolean test;
    private int iteration = 1;


    public Simulation(CLIENT client, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        this.client = client;
        this.randomSource = new RandomSource(randomSeed);
        this.agentConfigs = agentConfigs;
        this.iterationSamplingFunction = iterationSamplingFunction;
        this.world = world;
        this.test = test;
        this.agentReports = new ConcurrentHashMap<>();
        try {initialise(initialisationDataPaths);} catch (Exception exception) {
            exception.printStackTrace();
        }
        this.agents = agentListFromConfigs();
    }

    protected List<Agent<?, TX>> agentListFromConfigs() {
        List<Agent<?, TX>> agents = new ArrayList<>();
        ActionFactory<TX, ?> actionFactory = actionFactory();
        AgentFactory<TX, ?> agentFactory = new AgentFactory<>(client, actionFactory, this);

        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                Agent<?, TX> agent = agentFactory.get(agentConfig.getName());
                agent.overrideTracing(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    protected abstract ActionFactory<TX, ?> actionFactory();

    protected abstract void initialise(Map<String, Path> initialisationDataPaths) throws Exception;

    public void iterate() {
        agentReports.clear();
        for (Agent<?, ?> agent : agents) {
            agentReports.put(agent.getClass(), agent.iterate(randomSource.next()));
        }
        closeIteration();  // We want to test opening new sessions each iteration.
        iteration++;
    }

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
    public boolean isTracing() {
        return iterationSamplingFunction.apply(iteration());
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public Map<String, List<Action<?, ?>.Report>> getReport(Class<? extends Agent> agentName) {
        return agentReports.get(agentName);
    }

    public String printStatistics() {
        return client.printStatistics();
    }

    protected void closeIteration() {
        client.closeSessions();
    }

    public void close() {
        client.close();
    }
}
