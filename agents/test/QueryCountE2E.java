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

public class QueryCountE2E {

    private GraknClient graknClient;
    private final String KEYSPACE = "world";

    @Before
    public void createClient() {
        TestArgsInterpreter testArgsInterpreter = new TestArgsInterpreter();
        graknClient = new GraknClient(testArgsInterpreter.getHost());
    }

    private void assertQueryCount(GraqlGet.Aggregate countQuery, int expectedCount) {
        try (GraknClient.Session session = graknClient.session(KEYSPACE)) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                List<Numeric> answer = tx.execute(countQuery);
                int numAnswers = answer.get(0).number().intValue();
                assertThat(numAnswers, equalTo(expectedCount));
            }
        }
    }

    @Test
    public void testCompanyAgentInsertsTheExpectedNumberOfCompanies() {
        GraqlGet.Aggregate countQuery = Graql.match(
                var("country").isa("country")
                        .has("location-name", var("country-name")),
                var("company").isa("company")
                        .has("company-name", var("company-name"))
                        .has("company-number", var("company-number")),
                var("reg").isa("incorporation")
                        .rel("incorporation_incorporated", var("company"))
                        .rel("incorporation_incorporating", var("country"))
                        .has("date-of-incorporation", var("date-today"))
        ).get().count();
        assertQueryCount(countQuery, 350);
    }

    @Test
    public void testEmploymentAgentInsertsTheExpectedNumberOfEmployments() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var("emp").isa("employment")
                        .rel("employment_employee", Graql.var("p"))
                        .rel("employment_employer", Graql.var("company"))
                        .rel("employment_contract", Graql.var("contract"))
                        .has("start-date", Graql.var("employment-date"))
                        .has("annual-wage", Graql.var("annual-wage"), Graql.var("r")),
//                        Test times out when querying for currency too
//                Graql.var("r").has("currency", Graql.var("currency")), // TODO Should this be inferred rather than inserted?
                Graql.var("locates").isa("locates")
                        .rel("locates_located", Graql.var("emp"))
                        .rel("locates_location", Graql.var("city")),
                Graql.var("contract").isa("employment-contract")
                        .has("contract-content", Graql.var("contract-content"))
                        .has("contracted-hours", Graql.var("contracted-hours"))
        ).get().count();
        assertQueryCount(countQuery, 200);
    }

    @Test
    public void testMarriageAgentInsertsTheExpectedNumberOfMarriages() {
        GraqlGet.Aggregate countQuery = Graql.match(
                var("m").isa("marriage")
                        .rel("marriage_husband", "husband")
                        .rel("marriage_wife", "wife")
        ).get().count();
        assertQueryCount(countQuery, 116);
    }

    @Test
    public void testParentshipAgentInsertsTheExpectedNumberOfParentships() {
        // Note that that parentships with additional children will be counted a number of times equal to the number of children
        GraqlGet.Aggregate countQuery = Graql.match(
                var("p").isa("parentship")
                        .rel("parentship_parent", "p1")
                        .rel("parentship_parent", "p2")
                        .rel("parentship_child", "ch")
        ).get().count();
        assertQueryCount(countQuery, 530);
    }
    @Test
    public void testProductAgentInsertsTheExpectedNumberOfProducts() {
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

        assertQueryCount(countQuery, 300);
    }

    @Test
    public void testRelocationAgentInsertsTheExpectedNumberOfRelocations() {
        GraqlGet.Aggregate countQuery = Graql.match(
                var("r").isa("relocation")
                        .rel("relocation_previous-location", "l1")
                        .rel("relocation_new-location", "l2")
                        .rel("relocation_relocated-person", "p")
                        .has("relocation-date", Graql.var("d"))
        ).get().count();
        assertQueryCount(countQuery, 80);
    }

    @Test
    public void testTransactionAgentInsertsTheExpectedNumberOfTransactions() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var("product")
                        .isa("product")
                        .has("product-barcode", Graql.var("product-barcode")),
                Graql.var("c-buyer").isa("company")
                        .has("company-number", Graql.var("buyer-company-number")),
                Graql.var("c-seller").isa("company")
                        .has("company-number", Graql.var("seller-company-number")),
                Graql.var("country").isa("country")
                        .has("location-name", Graql.var("country-name")),
                Graql.var("transaction")
                        .isa("transaction")
                        .rel("transaction_vendor", Graql.var("c-seller"))
                        .rel("transaction_buyer", Graql.var("c-buyer"))
                        .rel("transaction_merchandise", Graql.var("product"))
//                        .has("currency", Graql.var("currency-name")) // TODO Requires reasoning over lots of results, test times out. Issue https://github.com/graknlabs/simulation/issues/40
                        .has("value", Graql.var("value"))
                        .has("product-quantity", Graql.var("quantity")),
                Graql.var("locates")
                        .isa("locates")
                        .rel("locates_location", Graql.var("country"))
                        .rel("locates_located", Graql.var("transaction"))
                ).get().count();
        assertQueryCount(countQuery, 9625);
    }

    @After
    public void closeClient() {
        graknClient.close();
    }
}
