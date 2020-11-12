package grakn.simulation.test;

import grakn.simulation.config.AgentMode;
import grakn.simulation.config.Config;
import grakn.simulation.config.SamplingFunction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.GraknSimulation;
import grakn.simulation.grakn.driver.GraknDriver;
import grakn.simulation.neo4j.Neo4jSimulation;
import grakn.simulation.neo4j.driver.Neo4jDriver;
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
import static grakn.simulation.common.world.World.initialise;

public class SimulationsForComparison {
    static final Neo4jSimulation neo4j;
    static final GraknSimulation grakn;
    static final int numIterations = 5;

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
        dirPaths.add("common/data");
        dirPaths.add("grakn/data");
        dirPaths.add("neo4j/data");

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
//        agentNames.add("parentship");
//        agentNames.add("relocation");
        agentNames.add("company");
        agentNames.add("employment");
//        agentNames.add("product");
//        agentNames.add("transaction");
//        agentNames.add("friendship");

        ArrayList<Config.Agent> agentConfigs = new ArrayList<>();
        agentNames.forEach(name -> agentConfigs.add(ConstructAgentConfig(name, AgentMode.RUN)));

        World world = initialise(scaleFactor, files);
        boolean test = true;

        /////////////////
        // Grakn setup //
        /////////////////

        grakn = new GraknSimulation(
                new GraknDriver(graknUri,"world"),
                files,
                randomSeed,
                world,
                agentConfigs,
                samplingFunction,
                test);

        /////////////////
        // Neo4j setup //
        /////////////////

        neo4j = new Neo4jSimulation(
                new Neo4jDriver(neo4jUri),
                files,
                randomSeed,
                world,
                agentConfigs,
                samplingFunction,
                test);
    }
}
