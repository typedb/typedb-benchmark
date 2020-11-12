package grakn.simulation.grakn.action.read;

import grakn.simulation.common.action.read.ResidentsInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.grakn.action.Model.CITY;
import static grakn.simulation.grakn.action.Model.EMAIL;
import static grakn.simulation.grakn.action.Model.END_DATE;
import static grakn.simulation.grakn.action.Model.LOCATION_NAME;
import static grakn.simulation.grakn.action.Model.PERSON;
import static grakn.simulation.grakn.action.Model.RESIDENCY;
import static grakn.simulation.grakn.action.Model.RESIDENCY_LOCATION;
import static grakn.simulation.grakn.action.Model.RESIDENCY_RESIDENT;
import static grakn.simulation.grakn.action.Model.START_DATE;

public class GraknResidentsInCityAction extends ResidentsInCityAction<GraknOperation> {

    public GraknResidentsInCityAction(GraknOperation dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public List<String> run() {
        return dbOperation.sortedExecute(query(city.name(), earliestDate), EMAIL, numResidents);
    }

    public static GraqlGet.Unfiltered query(String cityName, LocalDateTime earliestDate) {
        Statement person = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);
        Statement residency = Graql.var("r");
        Statement startDate = Graql.var(START_DATE);
        Statement endDate = Graql.var(END_DATE);
        return Graql.match(
                person.isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL)),
                cityVar
                        .isa(CITY).has(LOCATION_NAME, cityName),
                residency
                        .isa(RESIDENCY)
                        .rel(RESIDENCY_RESIDENT, PERSON)
                        .rel(RESIDENCY_LOCATION, CITY)
                        .has(START_DATE, startDate),
                Graql.not(
                        residency
                                .has(END_DATE, endDate)
                ),
                startDate.lte(earliestDate)
        ).get();
    }
}
