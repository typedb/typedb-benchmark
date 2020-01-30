package grakn.simulation.agents.test;

import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.Test;

import java.util.List;

import static graql.lang.Graql.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RelocationAgentE2E extends AgentE2E {

    @Test
    public void testParentshipAgentInsertsTheExpectedNumberOfParentships() {

        // Note that that parentships with additional children will be counted a number of times equal to the number of children
        localhostGraknTx(tx -> {
            GraqlGet.Aggregate parentshipsCountQuery = Graql.match(
                    var("r").isa("relocation")
                            .rel("relocation_previous-location", "l1")
                            .rel("relocation_new-location", "l2")
                            .rel("relocation_relocated-person", "p")
                            .has("relocation-date", Graql.var("d"))
            ).get().count();

            List<Numeric> answer = tx.execute(parentshipsCountQuery);
            int numParentships = answer.get(0).number().intValue();
            int expectedNumParentships = 100;
            assertThat(numParentships, equalTo(expectedNumParentships));
        });
    }
}
