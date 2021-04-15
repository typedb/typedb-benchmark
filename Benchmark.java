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

package grakn.benchmark;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.benchmark.common.params.Config;
import grakn.benchmark.common.params.Options;
import grakn.benchmark.grakn.GraknSimulation;
import grakn.benchmark.neo4j.Neo4JSimulation;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.common.params.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;

import static grakn.benchmark.common.Util.parseCommandLine;

public class Benchmark {

    final static Logger LOG = LoggerFactory.getLogger(Benchmark.class);

    public static void main(String[] args) {
        LOG.info("Welcome to the Benchmark!");

        Optional<Options> optionsOpt = parseCommandLine(args);
        if (optionsOpt.isEmpty()) System.exit(0);
        Options options = optionsOpt.get();

        try (GrablTracing ignore = initTracing(options.tracing().orElse(null), options.database().fullname())) {
            Config config = Config.loadYML(options.config());
            try (Simulation<?, ?, ?> simulation = initSimulation(options, config)) {
                simulation.run();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Simulation<?, ?, ?> initSimulation(Options options, Config config) throws Exception {
        Context context = Context.create(config, options.tracing().isPresent(), false);
        Simulation<?, ?, ?> simulation;
        if (options.database().isGraknCore()) simulation = GraknSimulation.core(options.address(), context);
        else if (options.database().isGraknCluster()) simulation = GraknSimulation.cluster(options.address(), context);
        else if (options.database().isNeo4j()) simulation = Neo4JSimulation.create(options.address(), context);
        else throw new IllegalStateException();
        return simulation;
    }

    private static GrablTracing initTracing(@Nullable Options.GrablTracing options, String analysisName) {
        GrablTracing tracing;
        if (options == null) return GrablTracing.createNoOp().withLogging();
        else if (options.credentials().isEmpty()) tracing = GrablTracing.create(options.grabl()).withLogging();
        else {
            Options.GrablTracing.Credentials cred = options.credentials().get();
            tracing = GrablTracing.create(options.grabl(), cred.username(), cred.token()).withLogging();
        }
        GrablTracingThreadStatic.setGlobalTracingClient(tracing);
        GrablTracingThreadStatic.openGlobalAnalysis(options.org(), options.repo(), options.commit(), analysisName);
        return tracing;
    }
}
