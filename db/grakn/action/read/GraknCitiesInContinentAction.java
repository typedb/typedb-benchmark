package grakn.simulation.db.grakn.action.read;

import grakn.simulation.db.common.action.read.CitiesInContinentAction;
import grakn.simulation.db.common.driver.TransactionalDbOperation;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;

public class GraknCitiesInContinentAction extends CitiesInContinentAction<GraknOperation> {
    public GraknCitiesInContinentAction(GraknOperation dbOperation, World.City city) {
        super(dbOperation, city);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var(CITY).isa(CITY).has(LOCATION_NAME, Graql.var("city-name")),
                Graql.var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, city.country().continent().name()),
                Graql.var("lh1").isa(LOCATION_HIERARCHY).rel(CITY).rel(CONTINENT),
                Graql.var("city-name").neq(city.name())
        ).get();
        return dbOperation.getOrderedAttribute(relocationCitiesQuery, "city-name", null);
    }
}
