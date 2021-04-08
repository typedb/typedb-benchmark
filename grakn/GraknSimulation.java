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

package grakn.benchmark.grakn;

import grakn.benchmark.config.Config;
import grakn.benchmark.grakn.action.GraknActionFactory;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknSession;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.grakn.loader.GraknYAMLLoader;
import grakn.benchmark.simulation.Simulation;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.loader.YAMLException;
import grakn.benchmark.simulation.loader.YAMLLoader;
import graql.lang.Graql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static grakn.benchmark.common.Util.printDuration;
import static grakn.client.api.GraknSession.Type.DATA;
import static grakn.client.api.GraknSession.Type.SCHEMA;

public class GraknSimulation extends Simulation<GraknClient, GraknSession, GraknTransaction> {

    public static final String DATABASE_NAME = "simulation";

    private static final Logger LOG = LoggerFactory.getLogger(GraknSimulation.class);

    private GraknSimulation(GraknClient client, Map<String, Path> initialisationDataPaths, int randomSeed,
                            List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        super(client, initialisationDataPaths, randomSeed, agentConfigs, context);
    }

    public static GraknSimulation core(String hostUri, Map<String, Path> initialisationDataPaths, int randomSeed,
                                       List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        return new GraknSimulation(GraknClient.core(hostUri, DATABASE_NAME), initialisationDataPaths, randomSeed, agentConfigs, context);
    }

    public static GraknSimulation cluster(String hostUri, Map<String, Path> initialisationDataPaths, int randomSeed,
                                          List<Config.Agent> agentConfigs, SimulationContext context) throws Exception {
        return new GraknSimulation(GraknClient.cluster(hostUri, DATABASE_NAME), initialisationDataPaths, randomSeed, agentConfigs, context);
    }

    @Override
    protected ActionFactory<GraknTransaction, ?> actionFactory() {
        return new GraknActionFactory();
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) throws Exception {
        grakn.client.api.GraknClient nativeClient = client().unpack();
        initialiseDatabase(nativeClient);
        initialiseSchema(nativeClient, initialisationDataPaths.get("schema.gql"));
        initialiseData(nativeClient, initialisationDataPaths);
    }

    private void initialiseDatabase(grakn.client.api.GraknClient nativeClient) {
        if (nativeClient.databases().contains(DATABASE_NAME)) nativeClient.databases().get(DATABASE_NAME).delete();
        nativeClient.databases().create(DATABASE_NAME);
    }

    private void initialiseSchema(grakn.client.api.GraknClient nativeClient, Path schemaFile) throws IOException {
        try (grakn.client.api.GraknSession session = nativeClient.session(DATABASE_NAME, SCHEMA)) {
            LOG.info("Grakn initialisation of world simulation schema started ...");
            Instant start = Instant.now();
            String schemaQuery = Files.readString(schemaFile);
            try (grakn.client.api.GraknTransaction tx = session.transaction(grakn.client.api.GraknTransaction.Type.WRITE)) {
                tx.query().define(Graql.parseQuery(schemaQuery));
                tx.commit();
            }
            LOG.info("Grakn initialisation of world simulation schema ended in {}", printDuration(start, Instant.now()));
        }
    }

    private void initialiseData(grakn.client.api.GraknClient nativeClient,
                                Map<String, Path> initialisationDataPaths) throws IOException, YAMLException {
        try (grakn.client.api.GraknSession session = nativeClient.session(DATABASE_NAME, DATA)) {
            LOG.info("Grakn initialisation of world simulation data started ...");
            Instant start = Instant.now();
            YAMLLoader loader = new GraknYAMLLoader(session, initialisationDataPaths);
            loader.loadFile(initialisationDataPaths.get("graql_templates.yml").toFile());
            LOG.info("Grakn initialisation of world simulation data ended in {}", printDuration(start, Instant.now()));
        }
    }
}
