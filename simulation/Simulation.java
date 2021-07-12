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
import com.vaticle.typedb.benchmark.simulation.agent.CitizenshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.CoupleFriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.FriendshipAgent;
import com.vaticle.typedb.benchmark.simulation.agent.GrandparenthoodAgent;
import com.vaticle.typedb.benchmark.simulation.agent.LineageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MaritalStatusAgent;
import com.vaticle.typedb.benchmark.simulation.agent.MarriageAgent;
import com.vaticle.typedb.benchmark.simulation.agent.NationalityAgent;
import com.vaticle.typedb.benchmark.simulation.agent.ParenthoodAgent;
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
                if (!agentBuilders.containsKey(agentClass)) {
                    throw new RuntimeException(String.format("%s is not registered as an agent",
                                                             agentConfig.getName()));
                }
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
            put(MarriageAgent.class, () -> createMarriageAgent(client, context));
            put(ParenthoodAgent.class, () -> createParenthoodAgent(client, context));
            put(MaritalStatusAgent.class, () -> createMaritalStatusAgent(client, context));
            put(GrandparenthoodAgent.class, () -> createGrandparenthoodAgent(client, context));
            put(LineageAgent.class, () -> createLineageAgent(client, context));
            put(NationalityAgent.class, () -> createNationalityAgent(client, context));
            put(CitizenshipAgent.class, () -> createCitizenshipAgent(client, context));
            put(CoupleFriendshipAgent.class, () -> createCoupleFriendshipAgent(client, context));
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
        agents.forEach(agent -> {
            Instant start = Instant.now();;
            Map<String, List<Agent.Report>> reports = agent.iterate(randomSource.nextSource());
            LOG.info("{} took: {}", agent.getClass().getSimpleName(), printDuration(start, Instant.now()));
            agentReports.put(agent.getClass().getSuperclass().getSimpleName(), reports);
        });
        context.incrementIteration();
    }

    @Override
    public void close() {
        client.close();
        context.close();
    }

    protected abstract PersonAgent<TX> createPersonAgent(CLIENT client, Context context);

    protected abstract FriendshipAgent<TX> createFriendshipAgent(CLIENT client, Context context);

    protected abstract MarriageAgent<TX> createMarriageAgent(CLIENT client, Context context);

    protected abstract ParenthoodAgent<TX> createParenthoodAgent(CLIENT client, Context context);

    protected abstract LineageAgent<TX> createLineageAgent(CLIENT client, Context context);

    protected abstract NationalityAgent<TX> createNationalityAgent(CLIENT client, Context context);

    protected abstract CitizenshipAgent<TX> createCitizenshipAgent(CLIENT client, Context context);

    protected abstract MaritalStatusAgent<TX> createMaritalStatusAgent(CLIENT client, Context context);

    protected abstract CoupleFriendshipAgent<TX> createCoupleFriendshipAgent(CLIENT client, Context context);

    protected abstract GrandparenthoodAgent<TX> createGrandparenthoodAgent(CLIENT client, Context context);

}
