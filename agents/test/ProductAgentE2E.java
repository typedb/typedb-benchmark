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

public class ProductAgentE2E {

    private GraknClient graknClient;
    private final String KEYSPACE = "world";

    @Before
    public void createClient() {
        TestArgsInterpreter testArgsInterpreter = new TestArgsInterpreter();
        graknClient = new GraknClient(testArgsInterpreter.getHost());
    }

    @Test
    public void testProductpAgentInsertsTheExpectedNumberOfProducts() {

        // Note that that parentships with additional children will be counted a number of times equal to the number of children
        try (GraknClient.Session session = graknClient.session(KEYSPACE)) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                GraqlGet.Aggregate countQuery = Graql.match(
                        Graql.var("continent")
                                .isa("continent")
                                .has("location-name", Graql.var("continent-name")),
                        Graql.var("product")
                                .isa("product")
                                .has("product-barcode", Graql.var("product-barcode"))
                                .has("product-name", Graql.var("product-name"))
                                .has("product-description", Graql.var("product-description")),
                        Graql.var("prod")
                                .isa("produced-in")
                                .rel("produced-in_product", Graql.var("product"))
                                .rel("produced-in_continent", Graql.var("continent"))
                ).get().count();

                List<Numeric> answer = tx.execute(countQuery);
                int numAnswers = answer.get(0).number().intValue();
                int expectedNumAnswers = 300;
                assertThat(numAnswers, equalTo(expectedNumAnswers));
            }
        }
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}
