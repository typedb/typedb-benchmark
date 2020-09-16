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

package grakn.simulation.grakn;

import grakn.client.Grakn;
import grakn.client.Grakn.Session;
import grakn.simulation.config.Config;
import grakn.simulation.common.TransactionalSimulation;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.world.World;
import grakn.simulation.common.yaml_tool.YAMLException;
import grakn.simulation.common.yaml_tool.YAMLLoader;
import grakn.simulation.grakn.action.GraknActionFactory;
import grakn.simulation.grakn.driver.GraknDriver;
import grakn.simulation.grakn.driver.GraknOperation;
import grakn.simulation.grakn.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GraknSimulation extends TransactionalSimulation<GraknDriver, GraknOperation> {

    public GraknSimulation(GraknDriver driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        super(driver, initialisationDataPaths, randomSeed, world, agentConfigs, iterationSamplingFunction, test);
    }

    @Override
    protected ActionFactory<GraknOperation, ?> actionFactory() {
        return new GraknActionFactory();
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        driver.createDatabase();
        try (Session schemaSession = driver.schemaSession("initialiseSchema")) {
            initialiseSchema(schemaSession, initialisationDataPaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Session dataSession = driver.session("initialiseData")) {
            initialiseData(dataSession, initialisationDataPaths);
        } catch (IOException | YAMLException e) {
            e.printStackTrace();
        }
    }

    private static void initialiseSchema(Grakn.Session session, Map<String, Path> initialisationDataPaths) throws IOException {
        System.out.println(">>>> trace: initialiseSchema: start");
        String schemaQuery = new String(Files.readAllBytes(initialisationDataPaths.get("schema.gql")), StandardCharsets.UTF_8);
        try (Grakn.Transaction tx = session.transaction(Grakn.Transaction.Type.WRITE)) {
            tx.query().define(Graql.parseQuery(schemaQuery));
            tx.commit();
        }
        System.out.println(">>>> trace: initialiseSchema: end");
    }

    private static void initialiseData(Grakn.Session session, Map<String, Path> initialisationDataPaths) throws IOException, YAMLException {
        System.out.println(">>>> trace: initialiseData: start");
        YAMLLoader loader = new GraknYAMLLoader(session, initialisationDataPaths);
        loader.loadFile(initialisationDataPaths.get("graql_templates.yml").toFile());
        System.out.println(">>>> trace: initialiseData: end");
    }
}
