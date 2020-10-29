package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.FindSpecificPersonAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.stream.Collectors;

import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknFindSpecificPersonAction extends FindSpecificPersonAction<GraknOperation> {
    public GraknFindSpecificPersonAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public String run() {
        GraqlGet.Unfiltered query = Graql.match(
                Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL)),
                Graql.var(EMAIL).isa(EMAIL).val(PERSON_EMAIL_FOR_QUERY)
        ).get();
        return optionalSingleResult(dbOperation.execute(query).stream().map(ans -> ans.get(EMAIL).asAttribute().value().toString()).collect(Collectors.toList()));
    }
}
