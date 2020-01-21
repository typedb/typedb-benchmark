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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class Simulation implements AgentContext {

    public static void main(String[] args) {
        System.out.println(StringPrettyBox.blocked("Welcome to the Simulation!"));
        System.out.println("Connecting to Grakn...");

        GraknClient client = new GraknClient();
        Session session = client.session("world");

        Simulation simulation = new Simulation(
                session,
                AgentList.AGENTS,
                new RandomSource(1)
        );

        try {
            simulation.loadSchema(Paths.get("schema/schema.gql"));
            simulation.loadData(Paths.get("data/data.yaml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        int times = 100;
        for (int i = 0; i < times; ++i) {
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
