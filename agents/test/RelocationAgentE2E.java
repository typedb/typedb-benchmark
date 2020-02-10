package grakn.simulation.agents.test;

import grakn.client.GraknClient;
import grakn.client.answer.Numeric;
import agents.test.common.TestArgsInterpreter;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static graql.lang.Graql.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RelocationAgentE2E {

    private GraknClient graknClient;
    private final String KEYSPACE = "world";

    @Before
    public void createClient() {
        TestArgsInterpreter testArgsInterpreter = new TestArgsInterpreter();
        graknClient = new GraknClient(testArgsInterpreter.getHost());
    }

    @Test
    public void testParentshipAgentInsertsTheExpectedNumberOfParentships() {

        // Note that that parentships with additional children will be counted a number of times equal to the number of children
        try (GraknClient.Session session = graknClient.session(KEYSPACE)) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                GraqlGet.Aggregate parentshipsCountQuery = Graql.match(
                        var("r").isa("relocation")
                                .rel("relocation_previous-location", "l1")
                                .rel("relocation_new-location", "l2")
                                .rel("relocation_relocated-person", "p")
                                .has("relocation-date", Graql.var("d"))
                ).get().count();

                List<Numeric> answer = tx.execute(parentshipsCountQuery);
                int numParentships = answer.get(0).number().intValue();
                int expectedNumParentships = 80;
                assertThat(numParentships, equalTo(expectedNumParentships));
            }
        }
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}
