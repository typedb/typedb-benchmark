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

package grakn.simulation.common.agent.base;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.common.action.Action;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.utils.Pair;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.utils.Trace;
import grakn.simulation.common.world.Region;
import grakn.simulation.common.world.World;
import grakn.simulation.common.utils.RandomSource;
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

/**
 * Agent constructs regional agents of a given class and runs them in parallel, providing them with the appropriate
 * region, a deterministic random and the tracker and session key for tracing and grakn transactions.
 *
 * This class must be extended to provide the source of the random items and the methods to obtain the session key and
 * tracker from them.
 *
 * @param <REGION> The type of region used by the agent.
 * @param <DB_OPERATION> The abstraction of database operations used by the agent.
 */
public abstract class Agent<REGION extends Region, DB_OPERATION extends DbOperation> {

    private final Logger logger;
    private boolean trace = true;
    private final DbDriver<DB_OPERATION> dbDriver;
    private final ActionFactory<DB_OPERATION, ?> actionFactory;
    private final Report report = new Report();

    protected Agent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected ActionFactory<DB_OPERATION, ?> actionFactory() {
        return actionFactory;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean trace() {
        return trace;
    }

    abstract protected List<REGION> getRegions(World world);

    public Report iterate(SimulationContext simulationContext, RandomSource randomSource) {
        List<REGION> regions = getRegions(simulationContext.world());
        List<RandomSource> randomSources = randomSource.split(regions.size());

        Pair.zip(randomSources, regions).parallelStream().forEach(
                pair -> iterateRegionalAgent(simulationContext, pair.getFirst(), pair.getSecond())
        );
        return report;
    }

    protected abstract RegionalAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test);

    private void iterateRegionalAgent(SimulationContext simulationContext, RandomSource source, REGION region) {
        Random random = source.startNewRandom();
        Random agentRandom = RandomSource.nextSource(random).startNewRandom();

        RegionalAgent regionalAgent = getRegionalAgent(simulationContext.simulationStep(), region.tracker(), agentRandom, simulationContext.test());
        DbOperationFactory<DB_OPERATION> dbOperationFactory = dbDriver.getDbOperationFactory(region, logger);

        RegionalAgent.Report report = regionalAgent.runWithReport(dbOperationFactory, region, simulationContext);
        this.report.addRegionalAgentReport(region.tracker(), report);
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public class Report {
        ConcurrentHashMap<String, RegionalAgent.Report> regionalAgentReports = new ConcurrentHashMap<>();

        public void addRegionalAgentReport(String tracker, RegionalAgent.Report regionalAgentReport) {
            regionalAgentReports.put(tracker, regionalAgentReport);
        }

        public Set<String> trackers() {
            return regionalAgentReports.keySet();
        }

        public RegionalAgent.Report getRegionalAgentReport(String tracker) {
            return regionalAgentReports.get(tracker);
        }
    }


    // TODO: Rename this to Regional, as well as the other Regional agents
    public abstract class RegionalAgent implements AutoCloseable {

        private final Random random;
        private final boolean test;
        private final Report report = new Report();
        private final String tracker;
        private GrablTracingThreadStatic.ThreadContext context;

        public RegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
            this.tracker = tracker;
            this.random = random;
            this.test = test;
            if (trace) {
                context = contextOnThread(tracker(), simulationStep);
            }
        }

        public String tracker() {
            return tracker;
        }

        protected Report runWithReport(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region, SimulationContext simulationContext) {
            Trace.trace(() -> {
                run(dbOperationFactory, region, simulationContext);
                return null;
            }, name(), trace);
            return report;
        }

        protected abstract void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, REGION region, SimulationContext simulationContext);

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
         * @param iterationScopeId An id that uniquely identifies a concept within the scope of the agent at a particular iteration
         * @return
         */
        public String uniqueId(SimulationContext simulationContext, int iterationScopeId) {
            return simulationContext.simulationStep() + "/" + tracker() + "/" + iterationScopeId;
        }

        public RandomValueGenerator randomAttributeGenerator() {
            return new RandomValueGenerator(random);
        }

        public <ACTION_RETURN_TYPE> ACTION_RETURN_TYPE runAction(Action<?, ACTION_RETURN_TYPE> action) {
            ACTION_RETURN_TYPE actionAnswer;
            actionAnswer = Trace.trace(action::run, action.name(), trace);
            if (test) {
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
