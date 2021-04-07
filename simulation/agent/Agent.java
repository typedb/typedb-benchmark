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

package grakn.benchmark.simulation.agent;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.common.RandomSource;
import grakn.benchmark.simulation.common.Region;
import grakn.benchmark.simulation.common.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
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
public abstract class Agent<REGION extends Region, TX extends Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    protected final SimulationContext context;
    private final Client<?, TX> client;
    private final ActionFactory<TX, ?> actionFactory;
    private boolean isTracing = true;

    protected Agent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        this.client = client;
        this.actionFactory = actionFactory;
        this.context = context;
    }

    protected abstract List<REGION> getRegions(World world);

    protected abstract List<Action<?, ?>.Report> run(Session<TX> session, REGION region, Random random);

    protected ActionFactory<TX, ?> actionFactory() {
        return actionFactory;
    }

    public void overrideTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }

    public boolean isTracing() {
        return context.isTracing() && isTracing;
    }

    public Map<String, List<Action<?, ?>.Report>> iterate(RandomSource randomSrc) {
        ConcurrentMap<String, List<Action<?, ?>.Report>> reports = new ConcurrentHashMap<>();
        List<REGION> regions = getRegions(context.world());
        regions.parallelStream().forEach(region -> {
            List<Action<?, ?>.Report> report = runWithMayTrace(region, randomSrc.next().get());
            if (context.isTest()) reports.put(region.tracker(), report);
        });
        return reports;
    }

    private List<Action<?, ?>.Report> runWithMayTrace(REGION region, Random random) {
        GrablTracingThreadStatic.ThreadContext tracingCtx = null;
        try {
            if (isTracing()) tracingCtx = contextOnThread(region.tracker(), context.iteration());
            Session<TX> session = client.session(region.sessionName());
            return mayTrace(() -> run(session, region, random), className(getClass()));
        } finally {
            if (tracingCtx != null) tracingCtx.close();
        }
    }

    public <T> T runAction(Action<?, T> action, List<Action<?, ?>.Report> reports) {
        T actionAnswer = mayTrace(action::run, action.name());
        if (context.isTest()) reports.add(action.report(actionAnswer));
        return actionAnswer;
    }

    public <U> U pickOne(List<U> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    public String uniqueId(SimulationContext benchmarkContext, String tracker, int iterationScopeId) {
        return benchmarkContext.iteration() + "/" + tracker + "/" + iterationScopeId;
    }

    public <T> T mayTrace(Supplier<T> methodToTrace, String trace) {
        if (isTracing()) {
            try (GrablTracingThreadStatic.ThreadTrace ignored = traceOnThread(trace)) {
                return methodToTrace.get();
            }
        } else {
            return methodToTrace.get();
        }
    }
}
