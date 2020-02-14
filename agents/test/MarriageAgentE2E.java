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

public class MarriageAgentE2E {

    private GraknClient graknClient;
    private final String KEYSPACE = "world";

    @Before
    public void createClient() {
        TestArgsInterpreter testArgsInterpreter = new TestArgsInterpreter();
        graknClient = new GraknClient(testArgsInterpreter.getHost());
    }

    @Test
    public void testMarriageAgentInsertsTheExpectedNumberOfMarriages() {
        try (GraknClient.Session session = graknClient.session(KEYSPACE)) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                GraqlGet.Aggregate marriagesCountQuery = Graql.match(
                        var("m").isa("marriage").rel("marriage_husband", "husband").rel("marriage_wife", "wife")
                ).get().count();

                List<Numeric> answer = tx.execute(marriagesCountQuery);
                int numMarriages = answer.get(0).number().intValue();
                int expectedNumMarriages = 115;
                assertThat(numMarriages, equalTo(expectedNumMarriages));
            }
        }
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}