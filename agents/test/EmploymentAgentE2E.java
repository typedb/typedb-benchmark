package grakn.simulation.agents.test;

import agents.test.common.TestArgsInterpreter;
import grakn.client.GraknClient;
import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EmploymentAgentE2E {

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
                        Graql.var("emp").isa("employment")
                                .rel("employment_employee", Graql.var("p"))
                                .rel("employment_employer", Graql.var("company"))
                                .rel("employment_contract", Graql.var("contract"))
                                .has("start-date", Graql.var("employment-date"))
                                .has("annual-wage", Graql.var("annual-wage"))
                ).get().count();

                List<Numeric> answer = tx.execute(parentshipsCountQuery);
                int numAnswers = answer.get(0).number().intValue();
                int expectedNumAnswers = 200;
                assertThat(numAnswers, equalTo(expectedNumAnswers));
            }
        }
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}
