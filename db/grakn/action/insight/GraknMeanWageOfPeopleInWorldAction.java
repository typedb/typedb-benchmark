package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;

import static grakn.simulation.db.grakn.schema.Schema.WAGE;
import static grakn.simulation.db.grakn.schema.Schema.WAGE_VALUE;

public class GraknMeanWageOfPeopleInWorldAction extends MeanWageOfPeopleInWorldAction<GraknOperation> {
    public GraknMeanWageOfPeopleInWorldAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public Double run() {
        return dbOperation.execute(query()).doubleValue();
    }

    public static GraqlGet.Aggregate query() {
        Statement wageValue = Graql.var(WAGE_VALUE);
        return Graql.match(
                Graql.var(WAGE).isa(WAGE)
                        .has(WAGE_VALUE, wageValue)
        ).get(wageValue.var()).mean(wageValue.var());
    }
}
