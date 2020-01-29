package grakn.simulation.agents.test;

import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.Test;

import java.util.List;

import static graql.lang.Graql.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MarriageAgentE2E extends AgentE2E{

    @Test
    public void testMarriageAgentInsertsTheExpectedNumberOfMarriages() {
        localhostGraknTx(tx -> {
            GraqlGet.Aggregate marriagesCountQuery = Graql.match(
                    var("m").isa("marriage").rel("marriage_husband", "husband").rel("marriage_wife", "wife")
            ).get().count();

            List<Numeric> answer = tx.execute(marriagesCountQuery);
            int numMarriages = answer.get(0).number().intValue();
            int expectedNumMarriages = 124;
            assertThat(numMarriages, equalTo(expectedNumMarriages));
        });
    }
}