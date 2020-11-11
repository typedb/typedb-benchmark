package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.CitiesInContinentAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.util.List;

import static grakn.simulation.grakn.schema.Schema.CITY;
import static grakn.simulation.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.grakn.schema.Schema.LOCATION_NAME;

public class GraknCitiesInContinentAction extends CitiesInContinentAction<GraknOperation> {
    public GraknCitiesInContinentAction(GraknOperation dbOperation, World.City city) {
        super(dbOperation, city);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered relocationCitiesQuery = query(city.name(), city.country().continent().name());
        return dbOperation.sortedExecute(relocationCitiesQuery, "city-name", null);
    }

    public static GraqlGet.Unfiltered query(String cityName, String continentName) {
        return Graql.match(
                    Graql.var(CITY).isa(CITY).has(LOCATION_NAME, Graql.var("city-name")),
                    Graql.var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, continentName),
                    Graql.var("lh1").isa(LOCATION_HIERARCHY).rel(CITY).rel(CONTINENT),
                    Graql.var("city-name").neq(cityName)
            ).get();
    }
}
