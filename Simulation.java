package grakn.simulation;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.simulation.agents.World;
import grakn.simulation.agents.base.IterationContext;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.driver.DbDriverWrapper;
import grakn.simulation.driver.GraknClientWrapper;
import grakn.simulation.common.RandomSource;
import grakn.simulation.config.Config;
import grakn.simulation.config.ConfigLoader;
import grakn.simulation.config.Schema;
import grakn.simulation.yaml_tool.GraknYAMLException;
import grakn.simulation.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static grabl.tracing.client.GrablTracing.tracing;
import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracing.withLogging;

public class Simulation implements IterationContext, AutoCloseable {

    private final static long RANDOM_SEED = 1;
    private final static int DEFAULT_NUM_ITERATIONS = 10;
    private final static int DEFAULT_SCALE_FACTOR = 5;
    private final static String DEFAULT_CONFIG_YAML = "config/config.yaml";
    private final static Logger LOG = LoggerFactory.getLogger(Simulation.class);

    private static Optional<String> getOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            return Optional.of(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    private static Options buildOptions() {
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

    public static void main(String[] args) {

        Options options = buildOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        String graknHostUri = getOption(commandLine, "u").orElse(GraknClient.DEFAULT_URI);
        String grablTracingUri = getOption(commandLine, "t").orElse("localhost:7979");
        String grablTracingOrganisation = commandLine.getOptionValue("o");
        String grablTracingRepository = commandLine.getOptionValue("r");
        String grablTracingCommit = commandLine.getOptionValue("c");
        String grablTracingUsername = commandLine.getOptionValue("u");
        String grablTracingToken = commandLine.getOptionValue("a");

        String dbName = getOption(commandLine, "d").orElse("grakn");

        Schema.Database db;
        switch (dbName) {
            case "grakn":
                db = Schema.Database.GRAKN;
                break;
            case "neo4j":
                db = Schema.Database.NEO4J;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dbName);
        }

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

        List<AgentRunner> agentRunners = new ArrayList<>();
        for (Config.Agent agent : config.getAgents()) {
            if (agent.getAgentMode().getRun()) {
                AgentRunner<?> runner = agent.getRunner();
                runner.setTrace(agent.getAgentMode().getTrace());
//                runner.setDb(db);// TODO Set the DB so that agent implementations are picked based on this
                agentRunners.add(runner);
            }
        }

        LOG.info("Welcome to the Simulation!");

        World world = initialiseWorld(scaleFactor, files);
        if (world == null) return;

        LOG.info(String.format("Connecting to %s...", db.toString()));

        try {
            GraknClientWrapper driverWrapper = null; // TODO inject this
            GrablTracing tracing = null;
            try {
                tracing = initialiseGrablTracing(grablTracingUri, grablTracingOrganisation, grablTracingRepository, grablTracingCommit, grablTracingUsername, grablTracingToken, disableTracing);

                driverWrapper = new GraknClientWrapper();
                driverWrapper.open(graknHostUri);

                initialiseForGrakn(databaseName, files, driverWrapper);

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
                if (driverWrapper != null) {
                    driverWrapper.close();
                }

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

    private static World initialiseWorld(int scaleFactor, Map<String, Path> files) {
        LOG.info("Parsing world data...");
        World world;
        try {
            world = new World(
                    scaleFactor,
                    files.get("continents.csv"),
                    files.get("countries.csv"),
                    files.get("cities.csv"),
                    files.get("female_forenames.csv"),
                    files.get("male_forenames.csv"),
                    files.get("surnames.csv"),
                    files.get("adjectives.csv"),
                    files.get("nouns.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        return world;
    }

    private static GrablTracing initialiseGrablTracing(String grablTracingUri, String grablTracingOrganisation, String grablTracingRepository, String grablTracingCommit, String grablTracingUsername, String grablTracingToken, boolean disableTracing) {
        GrablTracing tracing;
        if (disableTracing) {
            tracing = withLogging(tracingNoOp());
        } else if (grablTracingUsername == null) {
            tracing = withLogging(tracing(grablTracingUri));
        } else {
            tracing = withLogging(tracing(grablTracingUri, grablTracingUsername, grablTracingToken));
        }
        GrablTracingThreadStatic.setGlobalTracingClient(tracing);
        GrablTracingThreadStatic.openGlobalAnalysis(grablTracingOrganisation, grablTracingRepository, grablTracingCommit);
        return tracing;
    }

    private static void initialiseForGrakn(String graknKeyspace, Map<String, Path> files, GraknClientWrapper driver) throws IOException, GraknYAMLException {
        try (Session session = driver.getClient().session(graknKeyspace)) {
            // TODO: merge these two schema files once this issue is fixed
            // https://github.com/graknlabs/grakn/issues/5553
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("schema", 0)) {
                loadGraknSchema(session,
                        files.get("schema.gql"),
                        files.get("schema-pt2.gql"));
            }
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("data", 0)) {
                loadGraknData(session,
                        files.get("data.yaml"),
                        files.get("currencies.yaml"),
                        files.get("country_currencies.yaml"),
                        files.get("country_languages.yaml"));
            }
        }
    }

    private static void loadGraknSchema(Session session, Path... schemaPath) throws IOException {
        System.out.println(">>>> trace: loadSchema: start");
        for (Path path : schemaPath) {
            String schemaQuery = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            System.out.println(">>>> trace: loadSchema - session.tx.write: start");
            try (Transaction tx = session.transaction().write()) {
                System.out.println(">>>> trace: loadSchema - session.tx.write: end");
                tx.execute((GraqlDefine) Graql.parse(schemaQuery));
                tx.commit();
            }
        }
        System.out.println(">>>> trace: loadSchema: end");
    }

    private static void loadGraknData(Session session, Path... dataPath) throws IOException, GraknYAMLException {
        GraknYAMLLoader loader = new GraknYAMLLoader(session);
        for (Path path : dataPath) {
            loader.loadFile(path.toFile());
        }
    }

    private final DbDriverWrapper dbDriver;
    private final String keyspace;
    private final DbDriverWrapper.Session defaultSession;
    private final List<AgentRunner> agentRunners;
    private final Random random;
    private Function<Integer, Boolean> iterationSamplingFunction;
    private final World world;

    private int simulationStep = 1;

    private final ConcurrentMap<String, DbDriverWrapper.Session> sessionMap;

    private Simulation(DbDriverWrapper dbDriver, String keyspace, List<AgentRunner> agentRunners, RandomSource randomSource, World world, Function<Integer, Boolean> iterationSamplingFunction) {
        this.dbDriver = dbDriver;
        this.keyspace = keyspace;
        defaultSession = this.dbDriver.session(keyspace);
        this.agentRunners = agentRunners;
        random = randomSource.startNewRandom();
        this.iterationSamplingFunction = iterationSamplingFunction;
        sessionMap = new ConcurrentHashMap<>();
        this.world = world;
    }

    private void iterate() {

        LOG.info("Simulation step: {}", simulationStep);

        for (AgentRunner agentRunner : agentRunners) {
            agentRunner.iterate(this, RandomSource.nextSource(random));
        }

        closeAllSessionsInMap(); // We want to test opening new sessions each iteration.

        simulationStep++;
    }

    private void closeAllSessionsInMap() {
        for (DbDriverWrapper.Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }
    
    @Override
    public DbDriverWrapper.Session getIterationSessionFor(String key) {
        return sessionMap.computeIfAbsent(key, k -> dbDriver.session(keyspace)); // Open sessions for new keys
    }

    @Override
    public int getSimulationStep() {
        return simulationStep;
    }

    @Override
    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(LocalDate.ofYearDay(simulationStep, 1), LocalTime.of(0, 0, 0));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean shouldTrace() {
        return iterationSamplingFunction.apply(getSimulationStep());
    }

    @Override
    public void close() {
        closeAllSessionsInMap();

        defaultSession.close();

        if (dbDriver != null) {
            dbDriver.close();
        }
    }
}
