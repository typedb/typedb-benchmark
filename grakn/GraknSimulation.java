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

package grakn.benchmark.grakn;

import grakn.benchmark.config.Config;
import grakn.benchmark.grakn.action.GraknActionFactory;
import grakn.benchmark.grakn.driver.GraknClient;
import grakn.benchmark.grakn.driver.GraknSession;
import grakn.benchmark.grakn.driver.GraknTransaction;
import grakn.benchmark.grakn.loader.GraknYAMLLoader;
import grakn.benchmark.simulation.TransactionalSimulation;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.loader.YAMLException;
import grakn.benchmark.simulation.loader.YAMLLoader;
import grakn.benchmark.simulation.world.World;
import graql.lang.Graql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GraknSimulation extends TransactionalSimulation<GraknClient, GraknTransaction> {

    private static final Logger LOG = LoggerFactory.getLogger(GraknSimulation.class);

    public GraknSimulation(GraknClient driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world,
                           List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        super(driver, initialisationDataPaths, randomSeed, world, agentConfigs, iterationSamplingFunction, test);
    }

    @Override
    protected ActionFactory<GraknTransaction, ?> actionFactory() {
        return new GraknActionFactory();
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        client.createDatabase();
        try (GraknSession schemaSession = client.schemaSession("initialiseSchema")) {
            initialiseSchema(schemaSession.unpack(), initialisationDataPaths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (GraknSession dataSession = client.session("initialiseData")) {
            initialiseData(dataSession.unpack(), initialisationDataPaths);
        } catch (IOException | YAMLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initialiseSchema(grakn.client.api.GraknSession nativeSession,
                                         Map<String, Path> initialisationDataPaths) throws IOException {
        LOG.info(">>>> trace: initialiseSchema: start");
        String schemaQuery = new String(Files.readAllBytes(initialisationDataPaths.get("schema.gql")), StandardCharsets.UTF_8);
        try (grakn.client.api.GraknTransaction tx = nativeSession.transaction(grakn.client.api.GraknTransaction.Type.WRITE)) {
            tx.query().define(Graql.parseQuery(schemaQuery));
            tx.commit();
        }
        LOG.info(">>>> trace: initialiseSchema: end");
    }

    private static void initialiseData(grakn.client.api.GraknSession nativeSession,
                                       Map<String, Path> initialisationDataPaths) throws IOException, YAMLException {
        LOG.info(">>>> trace: initialiseData: start");
        YAMLLoader loader = new GraknYAMLLoader(nativeSession, initialisationDataPaths);
        loader.loadFile(initialisationDataPaths.get("graql_templates.yml").toFile());
        LOG.info(">>>> trace: initialiseData: end");
    }
}
