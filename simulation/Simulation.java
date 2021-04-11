/*
 * Copyright (C) 2021 Grakn Labs
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
import grakn.benchmark.simulation.agent.AgeUpdateAgent;
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.agent.ArbitraryOneHopAgent;
import grakn.benchmark.simulation.agent.CompanyAgent;
import grakn.benchmark.simulation.agent.EmploymentAgent;
import grakn.benchmark.simulation.agent.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.agent.FindLivedInAgent;
import grakn.benchmark.simulation.agent.FindSpecificMarriageAgent;
import grakn.benchmark.simulation.agent.FindSpecificPersonAgent;
import grakn.benchmark.simulation.agent.FindTransactionCurrencyAgent;
import grakn.benchmark.simulation.agent.FourHopAgent;
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.agent.MeanWageAgent;
import grakn.benchmark.simulation.agent.ParentshipAgent;
import grakn.benchmark.simulation.agent.PersonBirthAgent;
import grakn.benchmark.simulation.agent.ProductAgent;
import grakn.benchmark.simulation.agent.PurchaseAgent;
import grakn.benchmark.simulation.agent.RelocationAgent;
import grakn.benchmark.simulation.agent.ThreeHopAgent;
import grakn.benchmark.simulation.agent.TwoHopAgent;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.RandomSource;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Simulation<CLIENT extends Client<SESSION, TX>, SESSION extends Session<TX>, TX extends Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private static final String AGENT_PACKAGE = Agent.class.getPackageName();
    private final Map<Class<? extends Agent>, Agent<?, TX>> agentBuilders;

    private final CLIENT client;
    private final List<Agent<?, TX>> agents;
    private final RandomSource randomSource;
    private final List<Config.Agent> agentConfigs;
    private final SimulationContext context;
    private final Map<Class<? extends Agent>, Map<String, List<Action<?, ?>.Report>>> agentReports;

    public Simulation(CLIENT client, int seed, List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        this.client = client;
        this.randomSource = new RandomSource(seed);
        this.agentConfigs = agentConfigs;
        this.context = context;
        this.agentReports = new ConcurrentHashMap<>();
        this.agentBuilders = initialiseAgentBuilders();
        this.agents = agentListFromConfigs();
        initialiseDatabase();
        initialiseData(context.geoData());
    }

    protected abstract ActionFactory<TX, ?> actionFactory();

    protected abstract void initialiseDatabase() throws IOException;

    protected abstract void initialiseData(GeoData geoData);

    // TODO: make this static
    private Map<Class<? extends Agent>, Agent<?, TX>> initialiseAgentBuilders() {
        return new HashMap<>() {{
            put(MarriageAgent.class, new MarriageAgent<>(client(), actionFactory(), context));
            put(PersonBirthAgent.class, new PersonBirthAgent<>(client(), actionFactory(), context));
            put(AgeUpdateAgent.class, new AgeUpdateAgent<>(client(), actionFactory(), context));
            put(ParentshipAgent.class, new ParentshipAgent<>(client(), actionFactory(), context));
            put(RelocationAgent.class, new RelocationAgent<>(client(), actionFactory(), context));
            put(CompanyAgent.class, new CompanyAgent<>(client(), actionFactory(), context));
            put(EmploymentAgent.class, new EmploymentAgent<>(client(), actionFactory(), context));
            put(ProductAgent.class, new ProductAgent<>(client(), actionFactory(), context));
            put(PurchaseAgent.class, new PurchaseAgent<>(client(), actionFactory(), context));
            put(FriendshipAgent.class, new FriendshipAgent<>(client(), actionFactory(), context));
            put(MeanWageAgent.class, new MeanWageAgent<>(client(), actionFactory(), context));
            put(FindLivedInAgent.class, new FindLivedInAgent<>(client(), actionFactory(), context));
            put(FindCurrentResidentsAgent.class, new FindCurrentResidentsAgent<>(client(), actionFactory(), context));
            put(FindTransactionCurrencyAgent.class, new FindTransactionCurrencyAgent<>(client(), actionFactory(), context));
            put(ArbitraryOneHopAgent.class, new ArbitraryOneHopAgent<>(client(), actionFactory(), context));
            put(TwoHopAgent.class, new TwoHopAgent<>(client(), actionFactory(), context));
            put(ThreeHopAgent.class, new ThreeHopAgent<>(client(), actionFactory(), context));
            put(FourHopAgent.class, new FourHopAgent<>(client(), actionFactory(), context));
            put(FindSpecificMarriageAgent.class, new FindSpecificMarriageAgent<>(client(), actionFactory(), context));
            put(FindSpecificPersonAgent.class, new FindSpecificPersonAgent<>(client(), actionFactory(), context));
        }};
    }

    protected List<Agent<?, TX>> agentListFromConfigs() throws ClassNotFoundException {
        List<Agent<?, TX>> agents = new ArrayList<>();
        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                Agent<?, TX> agent;
                // TODO: should the agent names be identical to the class to begin with?
                String name = agentConfig.getName();
                name = AGENT_PACKAGE + "." + name.substring(0, 1).toUpperCase() + name.substring(1) + "Agent";
                Class<?> agentClass = Class.forName(name);
                if (agentBuilders.containsKey(agentClass)) agent = agentBuilders.get(agentClass);
                else throw new IllegalArgumentException("Unrecognised agent name: " + agentClass);
                agent.overrideTracing(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    public void iterate() {
        agentReports.clear();
        for (Agent<?, ?> agent : agents) {
            agentReports.put(agent.getClass(), agent.iterate(randomSource.next()));
        }
        // We want to test opening new sessions each iteration.
        client.closeSessions();
        context.incrementIteration();
    }

    public CLIENT client() {
        return client;
    }

    public Map<String, List<Action<?, ?>.Report>> getReport(Class<? extends Agent> agentName) {
        return agentReports.get(agentName);
    }

    public String printStatistics() {
        return client.printStatistics();
    }

    public void close() {
        client.close();
    }
}
