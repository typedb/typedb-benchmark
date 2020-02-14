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

import static graql.lang.Graql.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CompanyAgentE2E {
    private GraknClient graknClient;
    private final String KEYSPACE = "world";

    @Before
    public void createClient() {
        TestArgsInterpreter testArgsInterpreter = new TestArgsInterpreter();
        graknClient = new GraknClient(testArgsInterpreter.getHost());
    }

    @Test
    public void testCompanyAgentInsertsTheExpectedNumberOfCompanies() {
        try (GraknClient.Session session = graknClient.session(KEYSPACE)) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                GraqlGet.Aggregate countryCountQuery = Graql.match(
                        var("country").isa("country")
                                .has("name", var("country-name")),
                        var("company").isa("company")
                                .has("company-name", var("company-name"))
                                .has("company-number", var("company-number")),
                        var("reg").isa("incorporation")
                                .rel("incorporation_incorporated", var("company"))
                                .rel("incorporation_incorporating", var("country"))
                                .has("date-of-incorporation", var("date-today"))
                ).get().count();

                List<Numeric> answer = tx.execute(countryCountQuery);
                int num = answer.get(0).number().intValue();
                assertThat(num, equalTo(350));
            }
        }
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}
