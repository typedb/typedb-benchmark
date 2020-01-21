package grakn.simulation;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.simulation.agents.Agent;
import grakn.simulation.agents.AgentContext;
import grakn.simulation.common.RandomSource;
import grakn.simulation.common.StringPrettyBox;
import grakn.simulation.yaml_tool.GraknYAMLException;
import grakn.simulation.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class Simulation implements AgentContext {

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
        options.addOption(Option.builder()
                .argName("g").longOpt("grakn-uri").desc("Grakn server URI").hasArg()
                .build());
        options.addOption(Option.builder()
                .argName("s").longOpt("seed").desc("Simulation randomization seed").hasArg()
                .build());
        options.addOption(Option.builder()
                .argName("i").longOpt("iterations").desc("Number of simulation iterations").hasArg()
                .build());
        options.addOption(Option.builder()
                .argName("k").longOpt("keyspace").desc("Grakn keyspace").hasArg().required()
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        String graknHostUri = getOption(commandLine, "g").orElse(GraknClient.DEFAULT_URI);
        long seed = getOption(commandLine, "s").map(Long::parseLong).orElse(new Random().nextLong());
        int iterations = getOption(commandLine, "i").map(Integer::parseInt).orElse(10);
        String graknKeyspace = commandLine.getOptionValue("k");

        ////////////////////
        // INITIALIZATION //
        ////////////////////

        System.out.println(StringPrettyBox.blocked("Welcome to the Simulation!"));
        System.out.println("Connecting to Grakn...");

        GraknClient client = new GraknClient(graknHostUri);
        Session session = client.session(graknKeyspace);

        Simulation simulation = new Simulation(
                session,
                AgentList.AGENTS,
                new RandomSource(seed)
        );

        try {
            simulation.loadSchema(Paths.get("schema/schema.gql"));
            simulation.loadData(Paths.get("data/data.yaml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        ///////////////
        // MAIN LOOP //
        ///////////////

        for (int i = 0; i < iterations; ++i) {
            simulation.iterate();
        }
    }

    private final Session session;
    private final GraknYAMLLoader loader;
    private final Agent[] agents;
    private final Random random;

    private int simulationStep = 0;

    private Simulation(Session session, Agent[] agents, RandomSource randomSource) {
        this.session = session;
        this.loader = new GraknYAMLLoader(session);
        this.agents = agents;
        this.random = randomSource.startNewRandom();
    }

    private void iterate() {
        System.out.println(StringPrettyBox.simple("Simulation step: " + simulationStep, '*'));

        for (Agent agent : agents) {
            agent.iterate(this, RandomSource.nextSource(random));
        }

        simulationStep++;
    }

    private void loadSchema(Path schemaPath) throws IOException {
        String schemaQuery = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        try (Transaction tx = session.transaction().write()) {
            tx.execute((GraqlDefine) Graql.parse(schemaQuery));
            tx.commit();
        }
    }

    private void loadData(Path dataPath) throws IOException, GraknYAMLException {
        loader.loadFile(dataPath.toFile());
    }

    @Override
    public Session getGraknSession() {
        return session;
    }

    @Override
    public LocalDate getDate() {
        return LocalDate.ofEpochDay(simulationStep);
    }
}
