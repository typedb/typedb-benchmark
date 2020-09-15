package grakn.simulation.test;

import grakn.simulation.Simulation;
import grakn.simulation.config.Schema;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import grakn.simulation.db.grakn.initialise.GraknAgentPicker;
import grakn.simulation.db.grakn.initialise.GraknInitialiser;
import grakn.simulation.db.neo4j.driver.Neo4jDriverWrapper;
import grakn.simulation.db.neo4j.initialise.Neo4jAgentPicker;
import grakn.simulation.db.neo4j.initialise.Neo4jInitialiser;
import grakn.simulation.utils.RandomSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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

public class SimulationsUnderTest {
    static final Simulation neo4jSimulation;
    static final Simulation graknSimulation;
    static final int numIterations = 3;

    static {
        String[] args = System.getProperty("sun.java.command").split(" ");

        Options options = new Options();
        options.addOption(Option.builder("g")
                .longOpt("grakn-uri").desc("Grakn server URI").hasArg().required().argName("grakn-uri")
                .build());
        options.addOption(Option.builder("n")
                .longOpt("neo4j-uri").desc("Neo4j server URI").hasArg().required().argName("neo4j-uri")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        String graknUri = null;
        String neo4jUri = null;
        try {
            commandLine = parser.parse(options, args);
            graknUri = commandLine.getOptionValue("g");
            neo4jUri = commandLine.getOptionValue("n");
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

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

        GraknAgentPicker graknAgentPicker = new GraknAgentPicker();
        GraknInitialiser graknInitialiser = new GraknInitialiser(files);
        GraknClientWrapper graknDriverWrapper = new GraknClientWrapper();
        graknDriverWrapper.open(graknUri);

        List<AgentRunner<?>> graknAgentRunners = getAgentRunners(graknAgentPicker, agentNames);
        ResultHandler graknResultHandler = new ResultHandler();

        graknSimulation = new Simulation(
                graknDriverWrapper,
                "world",
                graknInitialiser,
                graknAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction,
                graknResultHandler
        );

        /////////////////
        // Neo4j setup //
        /////////////////

        Neo4jAgentPicker neo4jAgentPicker = new Neo4jAgentPicker();
        Neo4jInitialiser neo4jInitialiser = new Neo4jInitialiser(files);
        Neo4jDriverWrapper neo4jDriverWrapper = new Neo4jDriverWrapper();
        neo4jDriverWrapper.open(neo4jUri);

        List<AgentRunner<?>> neo4jAgentRunners = getAgentRunners(neo4jAgentPicker, agentNames);
        ResultHandler neo4jResultHandler = new ResultHandler();

        neo4jSimulation = new Simulation(
                neo4jDriverWrapper,
                "",
                neo4jInitialiser,
                neo4jAgentRunners,
                new RandomSource(randomSeed),
                world,
                samplingFunction,
                neo4jResultHandler
        );
    }

    private static List<AgentRunner<?>> getAgentRunners(AgentPicker agentPicker, List<String> agentNames) {
        List<AgentRunner<?>> agentRunners = new ArrayList<>();
        agentNames.forEach(agentName -> {
            AgentRunner<?> runner = agentPicker.get(agentName);
            agentRunners.add(runner);
        });
        return agentRunners;
    }
}
