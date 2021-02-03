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

package grakn.benchmark.common.agent.base;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.common.action.Action;
import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.utils.RandomSource;
import grakn.benchmark.common.utils.Utils;
import grakn.benchmark.common.world.Region;
import grakn.benchmark.common.world.World;
import grakn.common.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grakn.benchmark.common.utils.Trace.trace;
import static grakn.common.util.Objects.className;

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION>       The type of region used by the agent.
 * @param <DB_OPERATION> The abstraction of database operations used by the agent.
 */
public abstract class Agent<REGION extends Region, DB_OPERATION extends DbOperation> {

    protected final BenchmarkContext benchmarkContext;
    private final Logger logger;
    private final DbDriver<DB_OPERATION> dbDriver;
    private final ActionFactory<DB_OPERATION, ?> actionFactory;
    private final Report report = new Report();
    private boolean isTracing = true;

    protected Agent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, BenchmarkContext benchmarkContext) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        this.benchmarkContext = benchmarkContext;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected ActionFactory<DB_OPERATION, ?> actionFactory() {
        return actionFactory;
    }

    public void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }

    public boolean isTracing() {
        return benchmarkContext.trace() && isTracing;
    }

    abstract protected List<REGION> getRegions(World world);

    public Report iterate(RandomSource randomSource) {
        List<REGION> regions = getRegions(benchmarkContext.world());
        List<RandomSource> randomisers = randomSource.split(regions.size());

        trace(() -> {
                Utils.pairs(randomisers, regions).parallelStream().forEach(
                        pair -> executeRegionalAgent(pair.first(), pair.second())
                );
                return (Void) null;
        }, name(), isTracing());
        System.out.println(" name() in Report iterate is " + name());
        return report;
    }

    protected abstract Regional getRegionalAgent(int iteration, String tracker, Random random, boolean test);

    private void executeRegionalAgent(RandomSource source, REGION region) {
        Random random = source.get();
        Random agentRandom = RandomSource.nextSource(random).get();

        Regional regionalAgent = getRegionalAgent(benchmarkContext.iteration(), region.tracker(), agentRandom, benchmarkContext.test());
        DbOperationFactory<DB_OPERATION> dbOperationFactory = dbDriver.getDbOperationFactory(region, logger);

        Regional.Report report = regionalAgent.runWithReport(dbOperationFactory, region);
        this.report.addRegionalAgentReport(region.tracker(), report);
    }

    public String name() {
        return className(getClass());
    }

    public class Report {
        ConcurrentHashMap<String, Regional.Report> regionalAgentReports = new ConcurrentHashMap<>();

        public void addRegionalAgentReport(String tracker, Regional.Report regionalAgentReport) {
            regionalAgentReports.put(tracker, regionalAgentReport);
        }

        public Set<String> trackers() {
            return regionalAgentReports.keySet();
        }

        public Regional.Report getRegionalAgentReport(String tracker) {
            return regionalAgentReports.get(tracker);
        }
    }

    public abstract class Regional implements AutoCloseable {

        private final Random random;
        private final Report report = new Report();
        private final String tracker;
        private final boolean isTest;
        private final int iteration;
        private GrablTracingThreadStatic.ThreadContext context;

        public Regional(int iteration, String tracker, Random random, boolean isTest) {
            this.iteration = iteration;
            this.tracker = tracker;
            this.random = random;
            this.isTest = isTest;
            if (isTracing()) {
                context = contextOnThread(tracker(), iteration());
            }
        }

        public int iteration() {
            return iteration;
        }

        public String tracker() {
            return tracker;
        }

        protected Report runWithReport(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region) {
            trace(() -> {
                run(dbOperationFactory, region);
                return null;
            }, name() + "." + region.getClass().getSimpleName(), isTracing());
            System.out.println(" name() in Regional runWithReport is " + name() + "." + region.getClass().getSimpleName());
            return report;
        }

        protected abstract void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region);

        public <U> U pickOne(List<U> list) { // TODO can be a util
            return list.get(random().nextInt(list.size()));
        }

        public Random random() {
            return random;
        }

        protected void shuffle(List<?> list) {
            Collections.shuffle(list, random());
        }

        /**
         * Create a unique identifier, useful for creating keys without risk of collision
         *
         * @param iterationScopeId An id that uniquely identifies a concept within the scope of the agent at a particular iteration
         * @return
         */
        public String uniqueId(BenchmarkContext benchmarkContext, int iterationScopeId) {
            return benchmarkContext.iteration() + "/" + tracker() + "/" + iterationScopeId;
        }

        public RandomValueGenerator randomAttributeGenerator() {
            return new RandomValueGenerator(random);
        }

        public <ACTION_RETURN_TYPE> ACTION_RETURN_TYPE runAction(Action<?, ACTION_RETURN_TYPE> action) {
            ACTION_RETURN_TYPE actionAnswer;
            actionAnswer = trace(action::run, action.name(), isTracing());
            System.out.println(" action.name() in Regional runAction is " + action.name());
            if (isTest) {
                report.addActionReport(action.report(actionAnswer));
            }
            return actionAnswer;
        }

        public class Report {
            List<Action<?, ?>.Report> actionReports = new ArrayList<>();

            public void addActionReport(Action<?, ?>.Report actionReport) {
                actionReports.add(actionReport);
            }

            public Iterator<Action<?, ?>.Report> getActionReportIterator() {
                return actionReports.iterator();
            }
        }

        @Override
        public void close() {
            if (context != null) {
                context.close();
            }
        }
    }
}
