package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.FindCurrentResidentsAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.IS_CURRENT;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;

public class GraknFindCurrentResidentsAction extends FindCurrentResidentsAction<GraknOperation> {

    public GraknFindCurrentResidentsAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        return dbOperation.getOrderedAttribute(query(), EMAIL, null);
    }

    public static GraqlGet.Unfiltered query() {
        return Graql.match(
                    Graql.var(CITY).isa(CITY)
                            .has(LOCATION_NAME, "Berlin"),
                    Graql.var(RESIDENCY).isa(RESIDENCY)
                            .rel(RESIDENCY_LOCATION, Graql.var(CITY))
                            .rel(RESIDENCY_RESIDENT, Graql.var(PERSON))
                            .has(IS_CURRENT, true),
                    Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL))
            ).get();
    }
}
