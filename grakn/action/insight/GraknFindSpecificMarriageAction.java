package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.FindSpecificMarriageAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.stream.Collectors;

import static grakn.simulation.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.grakn.schema.Schema.MARRIAGE_ID;

public class GraknFindSpecificMarriageAction extends FindSpecificMarriageAction<GraknOperation> {
    public GraknFindSpecificMarriageAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        GraqlGet.Unfiltered query = query();
        return optionalSingleResult(dbOperation.execute(query).stream().map(ans -> ans.get(MARRIAGE_ID).asAttribute().value().toString()).collect(Collectors.toList()));
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(MARRIAGE).isa(MARRIAGE).has(MARRIAGE_ID, Graql.var(MARRIAGE_ID)),
                    Graql.var(MARRIAGE_ID).isa(MARRIAGE_ID).val(MARRIAGE_ID_FOR_QUERY)
            ).get();
    }
}
