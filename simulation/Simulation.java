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
import grakn.benchmark.simulation.agent.read.ArbitraryOneHopAgent;
import grakn.benchmark.simulation.agent.read.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.agent.read.FindLivedInAgent;
import grakn.benchmark.simulation.agent.read.FindSpecificMarriageAgent;
import grakn.benchmark.simulation.agent.read.FindSpecificPersonAgent;
import grakn.benchmark.simulation.agent.read.FindTransactionCurrencyAgent;
import grakn.benchmark.simulation.agent.read.FourHopAgent;
import grakn.benchmark.simulation.agent.read.MeanWageAgent;
import grakn.benchmark.simulation.agent.read.ThreeHopAgent;
import grakn.benchmark.simulation.agent.read.TwoHopAgent;
import grakn.benchmark.simulation.agent.write.AgeUpdateAgent;
import grakn.benchmark.simulation.agent.write.CompanyAgent;
import grakn.benchmark.simulation.agent.write.EmploymentAgent;
import grakn.benchmark.simulation.agent.write.FriendshipAgent;
import grakn.benchmark.simulation.agent.write.MarriageAgent;
import grakn.benchmark.simulation.agent.write.ParentshipAgent;
import grakn.benchmark.simulation.agent.write.PersonBirthAgent;
import grakn.benchmark.simulation.agent.write.ProductAgent;
import grakn.benchmark.simulation.agent.write.PurchaseAgent;
import grakn.benchmark.simulation.agent.write.RelocationAgent;
import grakn.benchmark.simulation.common.RandomSource;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Simulation<CLIENT extends Client<SESSION, TX>, SESSION extends Session<TX>, TX extends Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private final Map<Class<? extends Agent>, Function<SimulationContext, Agent<?, TX>>> agentBuilders;

    private final CLIENT client;
    private final List<Agent<?, TX>> agents;
    private final RandomSource randomSource;
    private final List<Config.Agent> agentConfigs;
    private final SimulationContext context;
    private final Map<Class<? extends Agent>, Map<String, List<Action<?, ?>.Report>>> agentReports;

    public Simulation(CLIENT client, Map<String, Path> initialisationDataPaths, int randomSeed, List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        this.client = client;
        this.randomSource = new RandomSource(randomSeed);
        this.agentConfigs = agentConfigs;
        this.context = context;
        this.agentReports = new ConcurrentHashMap<>();
        this.agentBuilders = initialiseAgentBuilders();
        this.agents = agentListFromConfigs();
        initialise(initialisationDataPaths);
    }

    protected abstract void initialise(Map<String, Path> initialisationDataPaths) throws Exception;

    protected abstract ActionFactory<TX, ?> actionFactory();

    // TODO: make this static
    private Map<Class<? extends Agent>, Function<SimulationContext, Agent<?, TX>>> initialiseAgentBuilders() {
        return new HashMap<>() {{
            put(MarriageAgent.class, context -> new MarriageAgent<>(client(), actionFactory(), context));
            put(PersonBirthAgent.class, context -> new PersonBirthAgent<>(client(), actionFactory(), context));
            put(AgeUpdateAgent.class, context -> new AgeUpdateAgent<>(client(), actionFactory(), context));
            put(ParentshipAgent.class, context -> new ParentshipAgent<>(client(), actionFactory(), context));
            put(RelocationAgent.class, context -> new RelocationAgent<>(client(), actionFactory(), context));
            put(CompanyAgent.class, context -> new CompanyAgent<>(client(), actionFactory(), context));
            put(EmploymentAgent.class, context -> new EmploymentAgent<>(client(), actionFactory(), context));
            put(ProductAgent.class, context -> new ProductAgent<>(client(), actionFactory(), context));
            put(PurchaseAgent.class, context -> new PurchaseAgent<>(client(), actionFactory(), context));
            put(FriendshipAgent.class, context -> new FriendshipAgent<>(client(), actionFactory(), context));
            put(MeanWageAgent.class, context -> new MeanWageAgent<>(client(), actionFactory(), context));
            put(FindLivedInAgent.class, context -> new FindLivedInAgent<>(client(), actionFactory(), context));
            put(FindCurrentResidentsAgent.class, context -> new FindCurrentResidentsAgent<>(client(), actionFactory(), context));
            put(FindTransactionCurrencyAgent.class, context -> new FindTransactionCurrencyAgent<>(client(), actionFactory(), context));
            put(ArbitraryOneHopAgent.class, context -> new ArbitraryOneHopAgent<>(client(), actionFactory(), context));
            put(TwoHopAgent.class, context -> new TwoHopAgent<>(client(), actionFactory(), context));
            put(ThreeHopAgent.class, context -> new ThreeHopAgent<>(client(), actionFactory(), context));
            put(FourHopAgent.class, context -> new FourHopAgent<>(client(), actionFactory(), context));
            put(FindSpecificMarriageAgent.class, context -> new FindSpecificMarriageAgent<>(client(), actionFactory(), context));
            put(FindSpecificPersonAgent.class, context -> new FindSpecificPersonAgent<>(client(), actionFactory(), context));
        }};
    }

    protected List<Agent<?, TX>> agentListFromConfigs() throws ClassNotFoundException {
        List<Agent<?, TX>> agents = new ArrayList<>();
        for (Config.Agent agentConfig : agentConfigs) {
            if (agentConfig.getAgentMode().getRun()) {
                Agent<?, TX> agent;
                Class<?> agentClass = Class.forName(agentConfig.getName() + "Agent");
                if (agentBuilders.containsKey(agentClass)) agent = agentBuilders.get(agentClass).apply(context);
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
