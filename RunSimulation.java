package grakn.simulation;

import grabl.tracing.client.GrablTracing;
import grakn.client.GraknClient;
import grakn.simulation.initialise.AgentPicker;
import grakn.simulation.driver.DriverWrapper;
import grakn.simulation.grakn.initialise.GraknAgentPicker;
import grakn.simulation.grakn.initialise.GraknInitialiser;
import grakn.simulation.initialise.Initialiser;
import grakn.simulation.world.World;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.common.RandomSource;
import grakn.simulation.config.Config;
import grakn.simulation.config.ConfigLoader;
import grakn.simulation.config.Schema;
import grakn.simulation.grakn.driver.GraknClientWrapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static grakn.simulation.initialise.Initialiser.grablTracing;
import static grakn.simulation.initialise.Initialiser.world;


public class RunSimulation {

    private final static long RANDOM_SEED = 1;
    private final static int DEFAULT_NUM_ITERATIONS = 10;
    private final static int DEFAULT_SCALE_FACTOR = 5;
    private final static String DEFAULT_CONFIG_YAML = "config/config.yaml";
    public final static Logger LOG = LoggerFactory.getLogger(Simulation.class);

    public static void main(String[] args) {

        Options options = cliOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        String dbName = getOption(commandLine, "d").orElse("grakn");
        String hostUri = getOption(commandLine, "u").orElse(null);
        String grablTracingUri = getOption(commandLine, "t").orElse("localhost:7979");
        String grablTracingOrganisation = commandLine.getOptionValue("o");
        String grablTracingRepository = commandLine.getOptionValue("r");
        String grablTracingCommit = commandLine.getOptionValue("c");
        String grablTracingUsername = commandLine.getOptionValue("u");
        String grablTracingToken = commandLine.getOptionValue("a");

        long seed = getOption(commandLine, "s").map(Long::parseLong).orElseGet(() -> {
            System.out.println("No seed supplied, using random seed: " + RANDOM_SEED);
            return RANDOM_SEED;
        });

        boolean disableTracing = commandLine.hasOption("z");

        int iterations = getOption(commandLine, "i").map(Integer::parseInt).orElse(DEFAULT_NUM_ITERATIONS);
        int scaleFactor = getOption(commandLine, "f").map(Integer::parseInt).orElse(DEFAULT_SCALE_FACTOR);
        String databaseName = commandLine.getOptionValue("k");

        Map<String, Path> files = new HashMap<>();
        for (String filepath : commandLine.getArgList()) {
            Path path = Paths.get(filepath);
            String filename = path.getFileName().toString();
            files.put(filename, path);
        }

        Path configPath = Paths.get(getOption(commandLine, "b").orElse(DEFAULT_CONFIG_YAML));
        Config config = ConfigLoader.loadConfigFromYaml(configPath.toFile());

        ////////////////////
        // INITIALIZATION //
        ////////////////////

        // Components customised based on the DB
        Schema.Database db;
        String defaultUri;
        AgentPicker agentPicker;
        Initialiser initialiser;
        DriverWrapper driverWrapper;
        switch (dbName) {
            case "grakn":
                db = Schema.Database.GRAKN;
                defaultUri = GraknClient.DEFAULT_URI;
                agentPicker = new GraknAgentPicker();
                initialiser = new GraknInitialiser();
                driverWrapper = new GraknClientWrapper();
                break;
//            case "neo4j":
//                db = Schema.Database.NEO4J;
//                defaultUri = "localhost:7474"; // TODO Check this
//                agentPicker = new Neo4jAgentPicker();
//                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + dbName);
        }

        if (hostUri == null) hostUri = defaultUri;


        // Get the agents with their runners
        List<AgentRunner> agentRunners = new ArrayList<>();
        for (Config.Agent agent : config.getAgents()) {
            if (agent.getAgentMode().getRun()) {
                AgentRunner<?> runner = agentPicker.get(agent.getName());
                runner.setTrace(agent.getAgentMode().getTrace());
//                runner.setDb(db);// TODO Set the DB so that agent implementations are picked based on this
                agentRunners.add(runner);
            }
        }

        LOG.info("Welcome to the Simulation!");
        LOG.info("Parsing world data...");
        World world = world(scaleFactor, files);
        if (world == null) return;

        LOG.info(String.format("Connecting to %s...", db.toString()));

        try {
            GrablTracing tracing = null;
            try {
                tracing = grablTracing(grablTracingUri, grablTracingOrganisation, grablTracingRepository, grablTracingCommit, grablTracingUsername, grablTracingToken, disableTracing);

                driverWrapper = new GraknClientWrapper();
                driverWrapper.open(hostUri);

                initialiser.initialise(driverWrapper, databaseName, files);

                try (Simulation simulation = new Simulation(
                        driverWrapper,
                        databaseName,
                        agentRunners,
                        new RandomSource(seed),
                        world,
                        config.getTraceSampling().getSamplingFunction()
                )) {
                    ///////////////
                    // MAIN LOOP //
                    ///////////////

                    for (int i = 0; i < iterations; ++i) {
                        simulation.iterate();
                    }
                }
            } finally {
                driverWrapper.close();

                if (tracing != null) {
                    tracing.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        LOG.info("Simulation complete");
    }

    private static Optional<String> getOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            return Optional.of(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    private static Options cliOptions() {
        Options options = new Options();
        options.addOption(Option.builder("d")
                .longOpt("database").desc("Database under test").hasArg().required().argName("database")
                .build());
        options.addOption(Option.builder("u")
                .longOpt("database-uri").desc("Database server URI").hasArg().argName("uri")
                .build());
        options.addOption(Option.builder("t")
                .longOpt("tracing-uri").desc("Grabl tracing server URI").hasArg().argName("uri")
                .build());
        options.addOption(Option.builder("o")
                .longOpt("org").desc("Repository organisation").hasArg().argName("name")
                .build());
        options.addOption(Option.builder("r")
                .longOpt("repo").desc("Grabl tracing repository").hasArg().argName("name")
                .build());
        options.addOption(Option.builder("c")
                .longOpt("commit").desc("Grabl tracing commit").hasArg().argName("sha")
                .build());
        options.addOption(Option.builder("u")
                .longOpt("username").desc("Grabl tracing username").hasArg().argName("username")
                .build());
        options.addOption(Option.builder("a")
                .longOpt("api-token").desc("Grabl tracing API token").hasArg().argName("token")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("seed").desc("Simulation randomization seed").hasArg().argName("seed")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("iterations").desc("Number of simulation iterations").hasArg().argName("iterations")
                .build());
        options.addOption(Option.builder("f")
                .longOpt("scale-factor").desc("Scale factor of iteration data").hasArg().argName("scale-factor")
                .build());
        options.addOption(Option.builder("k")
                .longOpt("keyspace").desc("keyspace name").hasArg().required().argName("name")
                .build());
        options.addOption(Option.builder("b")
                .longOpt("config-file").desc("Configuration file").hasArg().argName("config-file-path")
                .build());
        options.addOption(Option.builder("z")
                .longOpt("disable-tracing").desc("Disable grabl tracing")
                .build());
        return options;
    }
}
