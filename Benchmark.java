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

package com.vaticle.typedb.benchmark;

import com.vaticle.factory.tracing.client.FactoryTracing;
import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic;
import com.vaticle.typedb.benchmark.common.params.Config;
import com.vaticle.typedb.benchmark.common.params.Options;
import com.vaticle.typedb.benchmark.typedb.TypeDBSimulation;
import com.vaticle.typedb.benchmark.neo4j.Neo4JSimulation;
import com.vaticle.typedb.benchmark.simulation.Simulation;
import com.vaticle.typedb.benchmark.common.params.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.vaticle.typedb.benchmark.common.params.Options.parseCLIOptions;

public class Benchmark {

    final static Logger LOG = LoggerFactory.getLogger(Benchmark.class);

    public static void main(String[] args) {
        LOG.info("Welcome to the Benchmark!");

        Optional<Options> optionsOpt = Options.parseCLIOptions(args);
        if (optionsOpt.isEmpty()) System.exit(0);
        Options options = optionsOpt.get();

        try (FactoryTracing ignore = initTracing(options.tracing().orElse(null), options.database().fullname())) {
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
        if (options.database().isTypeDB()) simulation = TypeDBSimulation.core(options.address(), context);
        else if (options.database().isTypeDBCluster()) simulation = TypeDBSimulation.cluster(options.address(), context);
        else if (options.database().isNeo4j()) simulation = Neo4JSimulation.create(options.address(), context);
        else throw new IllegalStateException();
        return simulation;
    }

    private static FactoryTracing initTracing(@Nullable Options.FactoryTracing options, String databaseFullname) {
        FactoryTracing tracing;
        if (options == null) return FactoryTracing.createNoOp().withLogging();
        else if (options.credentials().isEmpty()) tracing = FactoryTracing.create(options.factory()).withLogging();
        else {
            Options.FactoryTracing.Credentials cred = options.credentials().get();
            tracing = FactoryTracing.create(options.factory(), cred.username(), cred.token()).withLogging();
        }
        FactoryTracingThreadStatic.setGlobalTracingClient(tracing);
        String analysisName = databaseFullname;
        if (options.scope() != null) analysisName = analysisName + "-" + options.scope();
        FactoryTracingThreadStatic.openGlobalAnalysis(options.org(), options.repo(), options.commit(), analysisName);
        return tracing;
    }
}
