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
import grakn.benchmark.simulation.common.RandomSource;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Simulation<CLIENT extends Client<SESSION, TX>, SESSION extends Session<TX>, TX extends Transaction> {

    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);

    private final List<Agent<?, TX>> agents;
    protected final CLIENT client;
    private final RandomSource randomSource;
    private final List<Config.Agent> agentConfigs;
    private final SimulationContext context;
    private final Map<Class<? extends Agent>, Map<String, List<Action<?, ?>.Report>>> agentReports;

    public Simulation(CLIENT client, Map<String, Path> initialisationDataPaths, int randomSeed, List<Config.Agent> agentConfigs, SimulationContext context) {
        this.client = client;
        this.randomSource = new RandomSource(randomSeed);
        this.agentConfigs = agentConfigs;
        this.context = context;
        this.agentReports = new ConcurrentHashMap<>();
        try {initialise(initialisationDataPaths);} catch (Exception exception) {
            exception.printStackTrace();
        }
        this.agents = agentListFromConfigs();
    }

    protected List<Agent<?, TX>> agentListFromConfigs() {
        List<Agent<?, TX>> agents = new ArrayList<>();
        ActionFactory<TX, ?> actionFactory = actionFactory();
        AgentFactory<TX, ?> agentFactory = new AgentFactory<>(client, actionFactory, context);

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
        context.incrementIteration();
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
