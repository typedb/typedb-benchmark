package grakn.simulation.agents.test;

import grakn.client.GraknClient;
import org.junit.After;
import org.junit.Before;

import java.util.function.Consumer;

public abstract class AgentE2E {

    private static final String[] args = System.getProperty("sun.java.command").split(" ");
    private GraknClient graknClient;

    @Before
    public void createClient() {
        String host;
        if (args.length == 1) {
            host = "localhost:48555";
        } else if (args.length == 2) {
            host = args[1];
        } else {
            throw new IllegalArgumentException("Received more arguments than expected. Accepts one argument, `grakn-uri`, or no arguments to use the default Grakn host.");
        }
        graknClient = new GraknClient(host);
    }

    @After
    public void closeClient() {
        graknClient.close();
    }

    void localhostGraknTx(Consumer<GraknClient.Transaction> fn) {
        String keyspace = "world";
        try (GraknClient.Session session = graknClient.session(keyspace)) {
            try (GraknClient.Transaction transaction = session.transaction().write()) {
                fn.accept(transaction);
            }
        }
    }
}