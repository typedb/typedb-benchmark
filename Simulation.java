package grakn.simulation;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.simulation.agents.Agent;
import grakn.simulation.agents.AgentContext;
import grakn.simulation.agents.World;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.StringPrettyBox;
import grakn.simulation.yaml_tool.GraknYAMLException;
import grakn.simulation.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Simulation implements AgentContext, AutoCloseable {

    private final static long RANDOM_SEED = 1;
    private final static int NUM_ITERATIONS = 10;

    private static Optional<String> getOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            return Optional.of(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {

        //////////////////////////
        // COMMAND LINE OPTIONS //
        //////////////////////////

        Options options = new Options();
        options.addOption(Option.builder("g")
                .longOpt("grakn-uri").desc("Grakn server URI").hasArg().argName("uri")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("seed").desc("Simulation randomization seed").hasArg().argName("seed")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("iterations").desc("Number of simulation iterations").hasArg().argName("iterations")
                .build());
        options.addOption(Option.builder("k")
                .longOpt("keyspace").desc("Grakn keyspace").hasArg().required().argName("keyspace")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        String graknHostUri = getOption(commandLine, "g").orElse(GraknClient.DEFAULT_URI);
        long seed = getOption(commandLine, "s").map(Long::parseLong).orElseGet(() -> {
            System.out.println("No seed supplied, using random seed: " + RANDOM_SEED);
            return RANDOM_SEED;
        });

        int iterations = getOption(commandLine, "i").map(Integer::parseInt).orElse(NUM_ITERATIONS);
        String graknKeyspace = commandLine.getOptionValue("k");

        ////////////////////
        // INITIALIZATION //
        ////////////////////

        System.out.println(StringPrettyBox.blocked("Welcome to the Simulation!"));
        System.out.println("Parsing world data...");
        World world;
        try {
            world = new World(
                    Paths.get("data/continents.csv"),
                    Paths.get("data/countries.csv"),
                    Paths.get("data/cities.csv"),
                    Paths.get("data/female_forenames.csv"),
                    Paths.get("data/male_forenames.csv"),
                    Paths.get("data/surnames.csv"),
                    Paths.get("/Users/jamesfletcher/programming/simulation/transaction-logs")
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        System.out.println("Connecting to Grakn...");
        try (Simulation simulation = new Simulation(
                    graknHostUri,
                    graknKeyspace,
                    AgentList.AGENTS,
                    new RandomSource(seed),
                    world
            )) {

            // TODO: merge these two schema files once this issue is fixed
            // https://github.com/graknlabs/grakn/issues/5553
            simulation.loadSchema(Paths.get("schema/schema.gql"));
            simulation.loadSchema(Paths.get("schema/schema-pt2.gql"));
            simulation.loadData(Paths.get("data/data.yaml"));
            simulation.loadData(Paths.get("data/currencies.yaml"));

            ///////////////
            // MAIN LOOP //
            ///////////////

            for (int i = 0; i < iterations; ++i) {
                simulation.iterate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private final GraknClient client;
    private final String keyspace;
    private final Session defaultSession;
    private final GraknYAMLLoader loader;
    private final Agent[] agents;
    private final Random random;
    private final World world;

    private int simulationStep = 0;

    private final ConcurrentMap<String, Session> sessionMap;

    private Simulation(String graknUri, String keyspace, Agent[] agents, RandomSource randomSource, World world) throws IOException {
        client = new GraknClient(graknUri);
        this.keyspace = keyspace;
        defaultSession = client.session(keyspace);
        loader = new GraknYAMLLoader(defaultSession);
        this.agents = agents;
        random = randomSource.startNewRandom();
        sessionMap = new ConcurrentHashMap<>();
        this.world = world;
    }

    private void iterate() {

        System.out.println(StringPrettyBox.simple("Simulation step: " + simulationStep, '*'));

        for (World.City city: this.world.getCities().collect(Collectors.toList())) {
            city.log(StringPrettyBox.simple("Simulation step: " + simulationStep, '*'));
        }

        for (Agent agent : agents) {
            agent.iterate(this, RandomSource.nextSource(random));
        }

        closeAllSessionsInMap(); // We want to test opening new sessions each iteration.

        simulationStep++;
    }

    private void loadSchema(Path schemaPath) throws IOException {
        String schemaQuery = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        try (Transaction tx = defaultSession.transaction().write()) {
            tx.execute((GraqlDefine) Graql.parse(schemaQuery));
            tx.commit();
        }
    }

    private void loadData(Path dataPath) throws IOException, GraknYAMLException {
        loader.loadFile(dataPath.toFile());
    }

    private void closeAllSessionsInMap() {
        for (Session session : sessionMap.values()) {
            session.close();
        }
        sessionMap.clear();
    }
    
    @Override
    public Session getIterationGraknSessionFor(String key) {
        return sessionMap.computeIfAbsent(key, k -> client.session(keyspace)); // Open sessions for new keys
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
    public void close() {
        closeAllSessionsInMap();

        defaultSession.close();

        if (client != null) {
            client.close();
        }

        for (World.City city: this.world.getCities().collect(Collectors.toList())) {
            city.close();
        }
    }
}
