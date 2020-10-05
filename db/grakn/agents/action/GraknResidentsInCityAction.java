package grakn.simulation.db.grakn.agents.action;

import grakn.simulation.db.common.agents.action.ResidentsInCityAction;
import grakn.simulation.db.common.agents.base.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknTransaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.END_DATE;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class GraknResidentsInCityAction extends ResidentsInCityAction<TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation> {

    public GraknResidentsInCityAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, World.City city, int numResidents, LocalDateTime earliestDate) {
        super(dbOperation, city, numResidents, earliestDate);
    }

    @Override
    public List<String> run() {
        Statement person = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);
        Statement residency = Graql.var("r");
        Statement startDate = Graql.var(START_DATE);
        Statement endDate = Graql.var(END_DATE);
        GraqlGet.Unfiltered cityResidentsQuery = Graql.match(
                person.isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL)),
                cityVar
                        .isa(CITY).has(LOCATION_NAME, city.name()),
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
        return dbOperation.tx().getOrderedAttribute(cityResidentsQuery, EMAIL, numResidents);
    }
}
