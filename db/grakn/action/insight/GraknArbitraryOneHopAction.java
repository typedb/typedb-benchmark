package grakn.simulation.db.grakn.action.insight;

import grakn.client.answer.ConceptMap;
import grakn.simulation.common.action.insight.ArbitraryOneHopAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknArbitraryOneHopAction extends ArbitraryOneHopAction<GraknOperation> {
    public GraknArbitraryOneHopAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Integer run() {
        List<ConceptMap> results = dbOperation.execute(query());
        return null;
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(PERSON).isa(PERSON).has(EMAIL, PERSON_EMAIL_FOR_QUERY),
                    Graql.var().rel(Graql.var(PERSON), Graql.var("x"))
            ).get("x");
    }
}
