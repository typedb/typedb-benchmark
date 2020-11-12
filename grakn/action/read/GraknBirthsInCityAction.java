package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.BirthsInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.grakn.action.Model.BORN_IN;
import static grakn.simulation.grakn.action.Model.BORN_IN_CHILD;
import static grakn.simulation.grakn.action.Model.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.DATE_OF_BIRTH;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;

public class GraknBirthsInCityAction extends BirthsInCityAction<GraknOperation> {
    public GraknBirthsInCityAction(GraknOperation dbOperation, World.City city, LocalDateTime today) {
        super(dbOperation, city, today);
    }

    @Override
    public List<String> run() {
        GraqlGet.Unfiltered childrenQuery = query(worldCity.name(), today);
        return dbOperation.sortedExecute(childrenQuery, EMAIL, null);
    }

    public static GraqlGet.Unfiltered query(String worldCityName, LocalDateTime today) {
        return Graql.match(
                    Graql.var("c").isa(CITY)
                            .has(LOCATION_NAME, worldCityName),
                    Graql.var("child").isa(PERSON)
                            .has(EMAIL, Graql.var(EMAIL))
                            .has(DATE_OF_BIRTH, today),
                    Graql.var("bi").isa(BORN_IN)
                            .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                            .rel(BORN_IN_CHILD, "child")
            ).get();
    }
}
