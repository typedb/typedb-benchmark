package grakn.simulation.grakn.action.insight;

import grakn.simulation.common.action.insight.MeanWageOfPeopleInWorldAction;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;

import static grakn.simulation.grakn.action.Model.WAGE;
import static grakn.simulation.grakn.action.Model.WAGE_VALUE;

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
