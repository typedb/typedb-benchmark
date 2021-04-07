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

package grakn.benchmark.simulation.agent.base;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.utils.RandomSource;
import grakn.benchmark.simulation.world.Region;
import grakn.benchmark.simulation.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grakn.benchmark.simulation.utils.Trace.trace;
import static grakn.common.util.Objects.className;

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION> The type of region used by the agent.
 * @param <TX>     The abstraction of database operations used by the agent.
 */
public abstract class AgentManager<REGION extends Region, TX extends Transaction> {

    protected final SimulationContext context;
    private final Logger logger;
    private final Client<TX> client;
    private final ActionFactory<TX, ?> actionFactory;
    private final ConcurrentMap<String, List<Action<?, ?>.Report>> reports = new ConcurrentHashMap<>();
    private boolean isTracing = true;

    protected AgentManager(Client<TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        this.client = client;
        this.actionFactory = actionFactory;
        this.context = context;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected ActionFactory<TX, ?> actionFactory() {
        return actionFactory;
    }

    public void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }

    public boolean isTracing() {
        return context.trace() && isTracing;
    }

    abstract protected List<REGION> getRegions(World world);

    public Map<String, List<Action<?, ?>.Report>> iterate(RandomSource randomSource) {
        List<REGION> regions = getRegions(context.world());
        regions.parallelStream().forEach(region -> executeAgent(region, randomSource.next()));
        return reports;
    }

    protected abstract Agent getAgent(REGION region, Random random, SimulationContext context);

    private void executeAgent(REGION region, RandomSource source) {
        Agent agent = getAgent(region, source.next().get(), context);
        Session<TX> session = client.session(region, logger);

        List<Action<?, ?>.Report> report = runWithReport(agent, session, region, agent.tracker(), agent.iteration());
        reports.put(region.tracker(), report);
    }

    public String name() {
        return className(getClass());
    }

    public <U> U pickOne(List<U> list, Random random) { // TODO can be a util
        return list.get(random.nextInt(list.size()));
    }

    public String uniqueId(SimulationContext benchmarkContext, String tracker, int iterationScopeId) {
        return benchmarkContext.iteration() + "/" + tracker + "/" + iterationScopeId;
    }

    public <ACTION_RETURN_TYPE> ACTION_RETURN_TYPE runAction(Action<?, ACTION_RETURN_TYPE> action, boolean isTest,
                                                             List<Action<?, ?>.Report> actionReports) {
        ACTION_RETURN_TYPE actionAnswer;
        actionAnswer = trace(action::run, action.name(), isTracing());
        if (isTest) actionReports.add(action.report(actionAnswer));
        return actionAnswer;
    }

    private List<Action<?, ?>.Report> runWithReport(Agent agent, Session<TX> session, REGION region, String tracker, int iteration) {
        GrablTracingThreadStatic.ThreadContext context = null;
        try {
            if (isTracing()) context = contextOnThread(tracker, iteration);
            trace(() -> {
                agent.run(session, region);
                return null;
            }, name(), isTracing());
            return agent.actionReports();
        } finally {
            if (context != null) context.close();
        }
    }

    public abstract class Agent {

        private final REGION region;
        private final Random random;
        private final SimulationContext context;
        private final List<Action<?, ?>.Report> actionReports;

        public Agent(REGION region, Random random, SimulationContext context) {
            this.region = region;
            this.random = random;
            this.context = context;
            this.actionReports = new ArrayList<>();
        }

        public int iteration() {
            return context.iteration();
        }

        public String tracker() {
            return region.tracker();
        }

        public Random random() {
            return random;
        }

        public boolean isTest() {
            return context.isTest();
        }

        public List<Action<?, ?>.Report> actionReports() {
            return actionReports;
        }

        protected abstract void run(Session<TX> session, REGION region);
    }
}
