package grakn.simulation;

import grakn.simulation.config.Schema;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import grakn.simulation.db.grakn.initialise.GraknAgentPicker;
import grakn.simulation.db.grakn.initialise.GraknInitialiser;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import grakn.simulation.db.neo4j.initialise.Neo4jAgentPicker;
import grakn.simulation.db.neo4j.initialise.Neo4jInitialiser;
import grakn.simulation.utils.RandomSource;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static grakn.simulation.db.common.initialise.Initialiser.world;

public class SimulationTest {

    private static List<AgentRunner> getAgentRunners(AgentPicker agentPicker, List<String> agentNames) {
        List<AgentRunner> agentRunners = new ArrayList<>();
        agentNames.forEach(agentName -> {
            AgentRunner<?> runner = agentPicker.get(agentName);
            agentRunners.add(runner);
        });
        return agentRunners;
    }

    @Test
    public void testNeo4jVsGrakn() {

        int numIterations = 5;
        int scaleFactor = 5;
        int randomSeed = 1;

        Function<Integer, Boolean> samplingFunction = Schema.SamplingFunction.applyArg(Schema.SamplingFunction.getByName("every"), 1);

        Map<String, Path> files = new HashMap<>();

        List<String> dirPaths = new ArrayList<>();
        dirPaths.add("db/grakn/schema");
        dirPaths.add("db/common/data");
        dirPaths.add("db/grakn/schema");
        dirPaths.add("db/grakn/data");
        dirPaths.add("db/neo4j/data");


        dirPaths.forEach(dirPath -> {
            Arrays.asList(Objects.requireNonNull(Paths.get(dirPath).toFile().listFiles())).forEach(file -> {
                Path path = file.toPath();
                String filename = path.getFileName().toString();
                files.put(filename, path);
            });
        });

        ArrayList<String> agentNames = new ArrayList<>();
        agentNames.add("marriage");
        agentNames.add("personBirth");
        agentNames.add("ageUpdate");

        World world = world(scaleFactor, files);

        /////////////////
        // Grakn setup //
        /////////////////

        String graknUri = "localhost:48555";
        GraknAgentPicker graknAgentPicker = new GraknAgentPicker();
        GraknInitialiser graknInitialiser = new GraknInitialiser(files);
        GraknClientWrapper graknDriverWrapper = new GraknClientWrapper();
        graknDriverWrapper.open(graknUri);

        List<AgentRunner> graknAgentRunners = getAgentRunners(graknAgentPicker, agentNames);

        Simulation graknSimulation = new Simulation(
                graknDriverWrapper,
                "world",
                graknInitialiser, graknAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction
        );

        /////////////////
        // Neo4j setup //
        /////////////////

        String neo4jUri = "bolt://localhost:7687";
        Neo4jAgentPicker neo4jAgentPicker = new Neo4jAgentPicker();
        Neo4jInitialiser neo4jInitialiser = new Neo4jInitialiser(files);
        Neo4jDriverWrapper neo4jDriverWrapper = new Neo4jDriverWrapper();
        neo4jDriverWrapper.open(neo4jUri);

        List<AgentRunner> neo4jAgentRunners = getAgentRunners(neo4jAgentPicker, agentNames);

        Simulation neo4jSimulation = new Simulation(
                neo4jDriverWrapper,
                "",
                neo4jInitialiser, neo4jAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction
        );

        /////////////////////
        // Run simulations //
        /////////////////////

        for (int i = 0; i < numIterations; ++i) {
            graknSimulation.iterate();
            neo4jSimulation.iterate();
        }

        ///////////
        // Close //
        ///////////

        neo4jSimulation.close();
        graknSimulation.close();
    }
}
