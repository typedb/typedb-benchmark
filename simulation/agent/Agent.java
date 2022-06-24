/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.benchmark.simulation.agent;

import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic;
import com.vaticle.typedb.benchmark.common.concept.Region;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static com.vaticle.factory.tracing.client.FactoryTracingThreadStatic.contextOnThread;
import static com.vaticle.factory.tracing.client.FactoryTracingThreadStatic.traceOnThread;
import static com.vaticle.typedb.common.collection.Collections.pair;
import static com.vaticle.typedb.common.util.Objects.className;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and TypeDB transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION> The type of region used by the agent.
 * @param <TX>     The abstraction of database operations used by the agent.
 */
public abstract class Agent<REGION extends Region, TX extends Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    protected final Context context;
    private final Client<?, TX> client;
    private boolean isTracing = true;

    protected Agent(Client<?, TX> client, Context context) {
        this.client = client;
        this.context = context;
    }

    protected abstract Class<? extends Agent> agentClass();

    protected abstract List<REGION> regions();

    protected abstract List<Report> run(Session<TX> session, REGION region, RandomSource random);

    public Agent<REGION, TX> setTracing(boolean isTracing) {
        this.isTracing = isTracing;
        return this;
    }

    public boolean isTracing() {
        return context.isTracing() && isTracing;
    }

    public Map<String, List<Report>> iterate(RandomSource randomSrc) {
        ConcurrentMap<String, List<Report>> reports = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> asyncRuns = new ArrayList<>(regions().size());
        // We need to generate pairs of Region and Random deterministically before passing them to a parallel stream
        regions().stream().map(r -> pair(r, randomSrc.nextSource())).forEach(rr -> asyncRuns.add(runAsync(() -> {
            List<Report> report = runAndMayTrace(rr.first(), rr.second());
            if (context.isReporting()) reports.put(rr.first().tracker(), report);
            else assert report.isEmpty();
        }, context.executor())));
        CompletableFuture.allOf(asyncRuns.toArray(new CompletableFuture[0])).join();
        return reports;
    }

    private List<Report> runAndMayTrace(REGION region, RandomSource random) {
        FactoryTracingThreadStatic.ThreadContext tracingCtx = null;
        try {
            if (isTracing()) tracingCtx = contextOnThread(region.tracker(), context.iterationNumber());
            Session<TX> session = client.session(region);
            return mayTrace(() -> run(session, region, random), className(agentClass()));
        } finally {
            if (tracingCtx != null) tracingCtx.close();
        }
    }

    public <T> T mayTrace(Supplier<T> methodToTrace, String trace) {
        if (isTracing()) {
            try (FactoryTracingThreadStatic.ThreadTrace ignored = traceOnThread(trace)) {
                return methodToTrace.get();
            }
        } else {
            return methodToTrace.get();
        }
    }

    public static class Report {

        private final Collection<Object> input;
        private final Collection<Object> output;
        private final int hash;

        public Report(Collection<Object> input, Collection<Object> output) {
            this.input = input;
            this.output = output;
            this.hash = Objects.hash(this.input, this.output);
        }

        public static Report create(Collection<Object> input, Collection<Object> output) {
            return new Report(input, output);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Report report = (Report) o;

            if (!input.equals(report.input)) return false;
            return output.equals(report.output);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
