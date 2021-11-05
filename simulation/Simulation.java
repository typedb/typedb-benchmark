/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.benchmark.simulation;

import com.vaticle.typedb.benchmark.common.params.Config;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.common.seed.SeedData;
import com.vaticle.typedb.benchmark.simulation.agent.Agent;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.PersonAgent;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.vaticle.typedb.benchmark.common.Util.printDuration;

public abstract class Simulation<
        CLIENT extends Client<SESSION, TX>,
        SESSION extends Session<TX>,
        TX extends Transaction> implements AutoCloseable {

    public static final Set<Class<? extends Agent>> REGISTERED_AGENTS = new HashSet<>();
    private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);
    private static final String AGENT_PACKAGE = Agent.class.getPackageName();

    protected final CLIENT client;
    protected final Context context;
    private final RandomSource randomSource;
    private final List<Agent<?, TX>> agents;
    private final Map<String, Map<String, List<Agent.Report>>> agentReports;

    public Simulation(CLIENT client, Context context) throws Exception {
        this.client = client;
        this.context = context;
        this.agents = initAgents();
        this.agentReports = new ConcurrentHashMap<>();
        this.randomSource = new RandomSource(context.seed());
        initialise(context.seedData());
    }

    protected abstract void initialise(SeedData geoData) throws IOException;

    @SuppressWarnings("unchecked")
    protected List<Agent<?, TX>> initAgents() throws ClassNotFoundException {
        Map<Class<? extends Agent>, Supplier<Agent<?, TX>>> agentBuilders = initAgentBuilders();
        List<Agent<?, TX>> agents = new ArrayList<>();
        for (Config.Agent agentConfig : context.agentConfigs()) {
            if (agentConfig.isRun()) {
                String className = AGENT_PACKAGE + "." + agentConfig.getName();
                Class<? extends Agent> agentClass = (Class<? extends Agent>) Class.forName(className);
                assert agentBuilders.containsKey(agentClass);
                agents.add(agentBuilders.get(agentClass).get().setTracing(agentConfig.isTracing()));
                REGISTERED_AGENTS.add(agentClass);
            }
        }
        return agents;
    }

    private Map<Class<? extends Agent>, Supplier<Agent<?, TX>>> initAgentBuilders() {
        return new HashMap<>() {{
            put(PersonAgent.class, () -> createPersonAgent(client, context));
            put(FriendshipAgent.class, () -> createFriendshipAgent(client, context));
        }};
    }

    public Map<String, List<Agent.Report>> getReport(Class<? extends Agent> agentName) {
        Map<String, List<Agent.Report>> report = agentReports.get(agentName.getSimpleName());
        assert report != null;
        return report;
    }

    public void run() {
        Instant start = Instant.now();
        while (context.iterationNumber() <= context.iterationMax()) {
            int iter = context.iterationNumber();
            Instant iterStart = Instant.now();
            iterate();
            LOG.info("Iteration {}: {}", iter, printDuration(iterStart, Instant.now()));
            LOG.info("-------------------------");
        }
        LOG.info("Simulation run duration: " + printDuration(start, Instant.now()));
        LOG.info(client.printStatistics());
    }

    public void iterate() {
        agentReports.clear();
        agents.forEach(agent -> agentReports.put(agent.getClass().getSuperclass().getSimpleName(), agent.iterate(randomSource.nextSource())));
        context.incrementIteration();
    }

    @Override
    public void close() {
        client.close();
        context.close();
    }

    protected abstract PersonAgent<TX> createPersonAgent(CLIENT client, Context context);

    protected abstract FriendshipAgent<TX> createFriendshipAgent(CLIENT client, Context context);
}
