package grakn.simulation.db.grakn.action.insight;

import grakn.simulation.db.common.action.insight.FindResidentsOfSpecificCityAction;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;

public class GraknFindResidentsOfSpecificCityAction extends FindResidentsOfSpecificCityAction<GraknOperation> {
    public GraknFindResidentsOfSpecificCityAction(GraknOperation dbOperation) {
        super(dbOperation);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered query = Graql.match(
                Graql.var(CITY).isa(CITY)
                        .has(LOCATION_NAME, "Berlin"),
                Graql.var(RESIDENCY).isa(RESIDENCY)
                        .rel(RESIDENCY_LOCATION, Graql.var(CITY))
                        .rel(RESIDENCY_RESIDENT, Graql.var(PERSON)),
                Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL))
        ).get();
        return dbOperation.getOrderedAttribute(query, EMAIL, null);
    }
}
