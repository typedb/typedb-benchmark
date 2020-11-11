package grakn.simulation.neo4j;

import grakn.simulation.config.Config;
import grakn.simulation.common.TransactionalSimulation;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.world.World;
import grakn.simulation.common.yaml_tool.YAMLException;
import grakn.simulation.common.yaml_tool.YAMLLoader;
import grakn.simulation.neo4j.action.Neo4jActionFactory;
import grakn.simulation.neo4j.driver.Neo4jDriver;
import grakn.simulation.neo4j.driver.Neo4jOperation;
import grakn.simulation.neo4j.yaml_tool.Neo4jYAMLLoader;
import org.neo4j.driver.Session;

import java.io.FileNotFoundException;
import java.nio.file.Path;
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
        YAMLLoader loader = new Neo4jYAMLLoader(session, initialisationDataPaths);
        try {
            loader.loadFile(initialisationDataPaths.get("neo4j_data.yml").toFile());
        } catch (YAMLException | FileNotFoundException e) {
            e.printStackTrace();
        }
        // TODO Add key constraints
        driver.closeSessions();
    }
}
