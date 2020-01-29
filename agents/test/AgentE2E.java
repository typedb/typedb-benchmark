package grakn.simulation.agents.test;

import grakn.client.GraknClient;
import org.junit.After;
import org.junit.Before;

import java.util.function.Consumer;

public abstract class AgentE2E {

    private GraknClient graknClient;

    @Before
    public void createClient() {
        String host = "localhost:48555";
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
