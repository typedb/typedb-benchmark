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

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.world.World;
import grakn.benchmark.common.yaml_tool.YAMLException;
import grakn.benchmark.common.yaml_tool.YAMLLoader;
import grakn.benchmark.config.Config;
import grakn.benchmark.grakn.action.GraknActionFactory;
import grakn.benchmark.grakn.driver.GraknDriver;
import grakn.benchmark.grakn.driver.GraknOperation;
import grakn.benchmark.grakn.yaml_tool.GraknYAMLLoader;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import graql.lang.Graql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GraknBenchmark extends grakn.benchmark.common.TransactionalBenchmark<GraknDriver, GraknOperation> {

    public GraknBenchmark(GraknDriver driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        super(driver, initialisationDataPaths, randomSeed, world, agentConfigs, iterationSamplingFunction, test);
    }

    @Override
    protected ActionFactory<GraknOperation, ?> actionFactory() {
        return new GraknActionFactory();
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        driver.createDatabase();
        try (GraknSession schemaSession = driver.schemaSession("initialiseSchema")) {
            initialiseSchema(schemaSession, initialisationDataPaths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (GraknSession dataSession = driver.session("initialiseData")) {
            initialiseData(dataSession, initialisationDataPaths);
        } catch (IOException | YAMLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initialiseSchema(GraknSession session, Map<String, Path> initialisationDataPaths) throws IOException {
        System.out.println(">>>> trace: initialiseSchema: start");
        String schemaQuery = new String(Files.readAllBytes(initialisationDataPaths.get("schema.gql")), StandardCharsets.UTF_8);
        try (GraknTransaction tx = session.transaction(GraknTransaction.Type.WRITE)) {
            tx.query().define(Graql.parseQuery(schemaQuery));
            tx.commit();
        }
        System.out.println(">>>> trace: initialiseSchema: end");
    }

    private static void initialiseData(GraknSession session, Map<String, Path> initialisationDataPaths) throws IOException, YAMLException {
        System.out.println(">>>> trace: initialiseData: start");
        YAMLLoader loader = new GraknYAMLLoader(session, initialisationDataPaths);
        loader.loadFile(initialisationDataPaths.get("graql_templates.yml").toFile());
        System.out.println(">>>> trace: initialiseData: end");
    }
}
