package grakn.simulation.test;

import grakn.simulation.config.AgentMode;
import grakn.simulation.config.Config;
import grakn.simulation.config.SamplingFunction;
import grakn.simulation.db.common.agents.base.ResultHandler;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.GraknSimulation;
import grakn.simulation.db.neo4j.Neo4jSimulation;
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

import static grakn.simulation.config.Config.Agent.ConstructAgentConfig;
import static grakn.simulation.db.common.world.World.initialise;

public class SimulationsUnderTest {
    static final Neo4jSimulation neo4jSimulation;
    static final GraknSimulation graknSimulation;
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

        Function<Integer, Boolean> samplingFunction = SamplingFunction.applyArg(SamplingFunction.getByName("every"), 1);

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
        agentNames.add("parentship");
//        agentNames.add("relocation");
        agentNames.add("company");
        agentNames.add("employment");
//        agentNames.add("product");
//        agentNames.add("transaction");
//        agentNames.add("friendship");

        ArrayList<Config.Agent> agentConfigs = new ArrayList<>();
        agentNames.forEach(name -> agentConfigs.add(ConstructAgentConfig(name, AgentMode.RUN)));

        World world = initialise(scaleFactor, files);

        /////////////////
        // Grakn setup //
        /////////////////

        ResultHandler graknResultHandler = new ResultHandler();
        boolean test = true;

        graknSimulation = new GraknSimulation(
                graknUri,
                "world",
                files,
                new RandomSource(randomSeed),
                world,
                agentConfigs,
                samplingFunction,
                graknResultHandler,
                test);

        /////////////////
        // Neo4j setup //
        /////////////////

        ResultHandler neo4jResultHandler = new ResultHandler();

        neo4jSimulation = new Neo4jSimulation(
                neo4jUri,
                files,
                new RandomSource(randomSeed),
                world,
                agentConfigs,
                samplingFunction,
                neo4jResultHandler,
                test);
    }
}
