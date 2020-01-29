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

public class ParentshipAgentE2E {

    private GraknClient graknClient;

    @Test
    public void testParentshipAgentInsertsTheExpectedNumberOfParentships() {

        // Note that that parentships with additional children will be counted a number of times equal to the number of children
        localhostGraknTx(tx -> {
            GraqlGet.Aggregate parentshipsCountQuery = Graql.match(
                    var("p").isa("parentship").rel("parentship_parent", "p1").rel("parentship_parent", "p2").rel("parentship_child", "ch")
            ).get().count();

            List<Numeric> answer = tx.execute(parentshipsCountQuery);
            int numParentships = answer.get(0).number().intValue();
            int expectedNumParentships = 520;
            assertThat(numParentships, equalTo(expectedNumParentships));
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
