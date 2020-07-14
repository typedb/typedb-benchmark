package grakn.simulation;

import grabl.tracing.client.GrablTracing;
import grakn.client.GraknClient;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.grakn.initialise.GraknAgentPicker;
import grakn.simulation.db.grakn.initialise.GraknInitialiser;
import grakn.simulation.db.common.initialise.Initialiser;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.utils.RandomSource;
import grakn.simulation.config.Config;
import grakn.simulation.config.ConfigLoader;
import grakn.simulation.config.Schema;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
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

import static grakn.simulation.db.common.initialise.Initialiser.grablTracing;
import static grakn.simulation.db.common.initialise.Initialiser.world;


public class RunSimulation {

    private final static String DEFAULT_CONFIG_YAML = "config/config.yaml";
    final static Logger LOG = LoggerFactory.getLogger(Simulation.class);

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

        boolean disableTracing = commandLine.hasOption("n");

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
                agentRunners.add(runner);
            }
        }

        LOG.info("Welcome to the Simulation!");
        LOG.info("Parsing world data...");
        World world = world(config.getScaleFactor(), files);
        if (world == null) return;

        LOG.info(String.format("Connecting to %s...", db.toString()));

        try {
            GrablTracing tracing = null;
            try {
                tracing = grablTracing(grablTracingUri, grablTracingOrganisation, grablTracingRepository, grablTracingCommit, grablTracingUsername, grablTracingToken, disableTracing);

                driverWrapper = new GraknClientWrapper();
                driverWrapper.open(hostUri);

                initialiser.initialise(driverWrapper, config.getDatabaseName(), files);

                try (Simulation simulation = new Simulation(
                        driverWrapper,
                        config.getDatabaseName(),
                        agentRunners,
                        new RandomSource(config.getRandomSeed()),
                        world,
                        config.getTraceSampling().getSamplingFunction()
                )) {
                    ///////////////
                    // MAIN LOOP //
                    ///////////////

                    for (int i = 0; i < config.getIterations(); ++i) {
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
        options.addOption(Option.builder("b")
                .longOpt("config-file").desc("Configuration file").hasArg().argName("config-file-path")
                .build());
        options.addOption(Option.builder("n")
                .longOpt("disable-tracing").desc("Disable grabl tracing")
                .build());
        return options;
    }
}
