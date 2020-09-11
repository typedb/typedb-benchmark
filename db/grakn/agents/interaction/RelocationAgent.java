package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.context.GraknContext;
import grakn.simulation.db.grakn.driver.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.CONTINENT;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.END_DATE;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_HIERARCHY;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_DATE;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_NEW_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_PREVIOUS_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RELOCATION_RELOCATED_PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class RelocationAgent extends grakn.simulation.db.common.agents.interaction.RelocationAgent<GraknContext> {

    private Transaction tx;

    @Override
    protected void openTx() {
        if (tx == null) {
            tx = backendContext().tx(getSessionKey());
        }
    }

    @Override
    protected void closeTx() {
        tx.close();
        tx = null;
    }

    @Override
    protected void commitTx() {
        tx.commit();
        tx = null;
    }

    static GraqlGet.Unfiltered cityResidentsQuery(World.City city, LocalDateTime earliestDate) {

        Statement person = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);
        Statement residency = Graql.var("r");
        Statement startDate = Graql.var(START_DATE);
        Statement endDate = Graql.var(END_DATE);

        return Graql.match(
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
    }

    @Override
    protected List<String> getResidentEmails(LocalDateTime earliestDate) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city(), earliestDate);
        log().query("getResidentEmails", cityResidentsQuery);
        int numRelocations = world().getScaleFactor();
        return tx.getOrderedAttribute(cityResidentsQuery, EMAIL, numRelocations);
    }

    @Override
    protected List<String> getRelocationCityNames() {

        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var(CITY).isa(CITY).has(LOCATION_NAME, Graql.var("city-name")),
                Graql.var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, city().country().continent().name()),
                Graql.var("lh1").isa(LOCATION_HIERARCHY).rel(CITY).rel(CONTINENT),
                Graql.var("city-name").neq(city().name())
        ).get();

        log().query("getRelocationCityNames", relocationCitiesQuery);
        return tx.getOrderedAttribute(relocationCitiesQuery, "city-name", null);
    }

    @Override
    protected void insertRelocation(String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var("p").isa(PERSON).has(EMAIL, email),
                Graql.var("new-city").isa(CITY).has(LOCATION_NAME, newCityName),
                Graql.var("old-city").isa(CITY).has(LOCATION_NAME, city().name())
        ).insert(
                Graql.var("r").isa(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, "p")
                        .has(RELOCATION_DATE, today())
        );

        log().query("insertRelocation", relocatePersonQuery);
        tx.execute(relocatePersonQuery);
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var(PERSON).isa(PERSON).has(EMAIL, Graql.var(EMAIL)),
                Graql.var("old-city").isa(CITY).has(LOCATION_NAME, city().name()),
                Graql.var(RESIDENCY).isa(RESIDENCY)
                        .rel(RESIDENCY_RESIDENT, PERSON)
                        .rel(RESIDENCY_LOCATION, "old-city")
                        .has(START_DATE, Graql.var(START_DATE)),
                Graql.not(Graql.var(RESIDENCY).has(END_DATE, Graql.var(END_DATE))),
//                Graql.var(START_DATE).lte(earliestDate),

                Graql.var("new-city").isa(CITY).has(LOCATION_NAME, Graql.var("newCityName")),
                Graql.var("old-city").isa(CITY).has(LOCATION_NAME, city().name()),

                Graql.var(RELOCATION).isa(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, PERSON)
                        .has(RELOCATION_DATE, today())
        ).get().count();
        log().query("checkCount", countQuery);
        return tx.count(countQuery);
    }

}