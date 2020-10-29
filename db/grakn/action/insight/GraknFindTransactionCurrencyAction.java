package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.FindTransactionCurrencyAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.simulation.db.grakn.schema.Schema.CURRENCY;
import static grakn.simulation.db.grakn.schema.Schema.TRANSACTION;

public class GraknFindTransactionCurrencyAction extends FindTransactionCurrencyAction<GraknOperation> {
    public GraknFindTransactionCurrencyAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered query = Graql.match(
                Graql.var(TRANSACTION).isa(TRANSACTION).has(CURRENCY, Graql.var(CURRENCY))
                ).get();
        return dbOperation.execute(query).stream().map(ans -> ans.get(CURRENCY).asAttribute().value().toString()).collect(Collectors.toList());
    }
}
