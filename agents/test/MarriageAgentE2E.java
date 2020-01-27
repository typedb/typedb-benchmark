package grakn.simulation.agents.test;

import grakn.client.GraknClient;
import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static graql.lang.Graql.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MarriageAgentE2E {

    private GraknClient graknClient;

    @Test
    public void clientJavaE2E() {
        localhostGraknTx(tx -> {
            GraqlGet.Aggregate marriagesCountQuery = Graql.match(
                    var("m").isa("marriage").rel("marriage_husband", "husband").rel("marriage_wife", "wife")
            ).get().count();

            List<Numeric> answer = tx.execute(marriagesCountQuery);
            int numMarriages = answer.get(0).number().intValue();
            int expectedNumMarriages = 143;
            assertThat(numMarriages, equalTo(expectedNumMarriages));
        });
    }

    @Before
    public void createClient() {
        String host = "localhost:48555";
        graknClient = new GraknClient(host);
    }

    @After
    public void closeClient() {
        graknClient.close();
    }

    private void localhostGraknTx(Consumer<GraknClient.Transaction> fn) {
        String keyspace = "world";
        try (GraknClient.Session session = graknClient.session(keyspace)) {
            try (GraknClient.Transaction transaction = session.transaction().write()) {
                fn.accept(transaction);
            }
        }
    }
}