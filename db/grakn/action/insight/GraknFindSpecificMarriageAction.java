package grakn.simulation.db.grakn.action.insight;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.action.insight.FindSpecificMarriageAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;

public class GraknFindSpecificMarriageAction extends FindSpecificMarriageAction<GraknOperation> {
    public GraknFindSpecificMarriageAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        GraqlGet.Unfiltered query = Graql.match(
                Graql.var().isa(MARRIAGE).has(Graql.var(MARRIAGE_ID)),
                Graql.var(MARRIAGE_ID).isa(MARRIAGE_ID).val(MARRIAGE_ID_FOR_QUERY)
        ).get();
        ConceptMap answer = singleResult(dbOperation.execute(query));
        return answer.get(MARRIAGE_ID).asAttribute().value().toString();
    }
}
