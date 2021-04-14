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

import grakn.benchmark.common.Config;
import grakn.benchmark.simulation.agent.Action;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static grakn.benchmark.common.Util.printDuration;

public abstract class Simulation<
        CLIENT extends Client<SESSION, TX>,
        SESSION extends Session<TX>,
        TX extends Transaction> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private static final String AGENT_PACKAGE = Agent.class.getPackageName();
    private final Map<Class<? extends Agent>, Supplier<Agent<?, TX>>> agentBuilders;

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

    protected abstract void initialiseDatabase() throws IOException;

    protected abstract void initialiseData(GeoData geoData);

    public CLIENT client() {
        return client;
    }

    public SimulationContext context() {
        return context;
    }

    public Map<String, List<Action<?, ?>.Report>> getReport(Class<? extends Agent> agentName) {
        return agentReports.get(agentName);
    }

    private Map<Class<? extends Agent>, Supplier<Agent<?, TX>>> initialiseAgentBuilders() {
        return new HashMap<>() {{
            put(AgeUpdateAgent.class, () -> createAgeUpdateAgent());
            put(ArbitraryOneHopAgent.class, () -> createArbitraryOneHopAgent());
            put(CompanyAgent.class, () -> createCompanyAgent());
            put(EmploymentAgent.class, () -> createEmploymentAgent());
            put(FindCurrentResidentsAgent.class, () -> createFindCurrentResidentsAgent());
            put(FindLivedInAgent.class, () -> createFindLivedInAgent());
            put(FindSpecificMarriageAgent.class, () -> createFindSpecificMarriageAgent());
            put(FindSpecificPersonAgent.class, () -> createFindSpecificPersonAgent());
            put(FindTransactionCurrencyAgent.class, () -> createFindTransactionCurrencyAgent());
            put(FourHopAgent.class, () -> createFourHopAgent());
            put(FriendshipAgent.class, () -> createFriendshipAgent());
            put(MarriageAgent.class, () -> createMarriageAgent());
            put(MeanWageAgent.class, () -> createMeanWageAgent());
            put(ParentshipAgent.class, () -> createParentshipAgent());
            put(PersonBirthAgent.class, () -> createPersonBirthAgent());
            put(ProductAgent.class, () -> createProductAgent());
            put(PurchaseAgent.class, () -> createPurchaseAgent());
            put(RelocationAgent.class, () -> createRelocationAgent());
            put(ThreeHopAgent.class, () -> createThreeHopAgent());
            put(TwoHopAgent.class, () -> createTwoHopAgent());
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
                if (agentBuilders.containsKey(agentClass)) agent = agentBuilders.get(agentClass).get();
                else throw new IllegalArgumentException("Unrecognised agent name: " + agentClass);
                agent.overrideTracing(agentConfig.getAgentMode().getTrace());
                agents.add(agent);
            }
        }
        return agents;
    }

    public void run() {
        Instant start = Instant.now();
        for (int i = 0; i < context.iterationMax(); i++) {
            Instant iterStart = Instant.now();
            iterate();
            Instant iterEnd = Instant.now();
            LOG.info("Iteration {}: {}", i, printDuration(iterStart, iterEnd));
        }
        LOG.info("Simulation run duration: " + printDuration(start, Instant.now()));
        LOG.info(client.printStatistics());
    }

    public void iterate() {
        agentReports.clear();
        for (Agent<?, ?> agent : agents) {
            agentReports.put(agent.getClass(), agent.iterate(randomSource.next()));
        }
        // We want to test.md opening new sessions each iteration.
        client.closeSessions();
        context.incrementIteration();
    }

    @Override
    public void close() {
        client.close();
    }

    protected abstract Agent<?, TX> createAgeUpdateAgent();

    protected abstract Agent<?, TX> createArbitraryOneHopAgent();

    protected abstract Agent<?, TX> createCompanyAgent();

    protected abstract Agent<?, TX> createEmploymentAgent();

    protected abstract Agent<?, TX> createFindCurrentResidentsAgent();

    protected abstract Agent<?, TX> createFindLivedInAgent();

    protected abstract Agent<?, TX> createFindSpecificMarriageAgent();

    protected abstract Agent<?, TX> createFindSpecificPersonAgent();

    protected abstract Agent<?, TX> createFindTransactionCurrencyAgent();

    protected abstract Agent<?, TX> createFourHopAgent();

    protected abstract Agent<?, TX> createFriendshipAgent();

    protected abstract Agent<?, TX> createMarriageAgent();

    protected abstract Agent<?, TX> createMeanWageAgent();

    protected abstract Agent<?, TX> createParentshipAgent();

    protected abstract Agent<?, TX> createPersonBirthAgent();

    protected abstract Agent<?, TX> createProductAgent();

    protected abstract Agent<?, TX> createPurchaseAgent();

    protected abstract Agent<?, TX> createRelocationAgent();

    protected abstract Agent<?, TX> createThreeHopAgent();

    protected abstract Agent<?, TX> createTwoHopAgent();
}
