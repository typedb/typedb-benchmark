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

package grakn.simulation.neo4j;

import grakn.simulation.common.TransactionalSimulation;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.world.World;
import grakn.simulation.common.yaml_tool.YAMLException;
import grakn.simulation.common.yaml_tool.YAMLLoader;
import grakn.simulation.config.Config;
import grakn.simulation.neo4j.action.Neo4jActionFactory;
import grakn.simulation.neo4j.driver.Neo4jDriver;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import grakn.simulation.neo4j.yaml_tool.Neo4jYAMLLoader;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Neo4jSimulation extends TransactionalSimulation<Neo4jDriver, Neo4jOperation> {

    public Neo4jSimulation(Neo4jDriver driver, Map<String, Path> initialisationDataPaths, int randomSeed, World world, List<Config.Agent> agentConfigs, Function<Integer, Boolean> iterationSamplingFunction, boolean test) {
        super(driver, initialisationDataPaths, randomSeed, world, agentConfigs, iterationSamplingFunction, test);
    }

    @Override
    protected ActionFactory<Neo4jOperation, ?> actionFactory() {
        return new Neo4jActionFactory();
    }

    @Override
    protected void initialise(Map<String, Path> initialisationDataPaths) {
        Session session = driver.session("initialise");
        addKeyConstraints(session);
        YAMLLoader loader = new Neo4jYAMLLoader(session, initialisationDataPaths);
        try {
            loader.loadFile(initialisationDataPaths.get("cypher_templates.yml").toFile());
        } catch (YAMLException | FileNotFoundException e) {
            throw new RuntimeException(e);

        }
        driver.closeSessions();
    }

    /**
     * Neo4j Community can create only uniqueness constraints, and only on nodes, not relationships. This means that it
     * does not enforce the existence of a property on those nodes. `exists()` is only available in Neo4j Enterprise.
     * https://neo4j.com/developer/kb/how-to-implement-a-primary-key-property-for-a-label/
     * @param session
     */
    private void addKeyConstraints(Session session) {
        List<String> queries = new ArrayList<String>() {{
            add("CREATE CONSTRAINT ON (person:Person) ASSERT person.email IS UNIQUE");
            add("CREATE CONSTRAINT ON (location:Location) ASSERT location.locationName IS UNIQUE");
            add("CREATE CONSTRAINT ON (company:Company) ASSERT company.companyName IS UNIQUE");
            add("CREATE CONSTRAINT ON (company:Company) ASSERT company.companyNumber IS UNIQUE");
            add("CREATE CONSTRAINT ON (product:Product) ASSERT product.productBarcode IS UNIQUE");
        }};
        Transaction tx = session.beginTransaction();
        for (String query : queries) {
            tx.run(new Query(query));
        }
        tx.commit();
    }
}
