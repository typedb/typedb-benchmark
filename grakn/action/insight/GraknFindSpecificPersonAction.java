package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.stream.Collectors;

import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknFindSpecificPersonAction extends FindSpecificPersonAction<GraknOperation> {
    public GraknFindSpecificPersonAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        return optionalSingleResult(dbOperation.execute(query()).stream().map(ans -> ans.get(EMAIL).asAttribute().value().toString()).collect(Collectors.toList()));
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL)),
                    Graql.var(EMAIL).isa(EMAIL).val(PERSON_EMAIL_FOR_QUERY)
            ).get();
    }
}
