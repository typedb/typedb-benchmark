package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;
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

public abstract class RelocationAgent<DB_DRIVER extends DbDriver> extends Agent<World.City, DB_DRIVER> {

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
    public List<String> getResidentEmails(World.City city, LocalDateTime earliestDate, int numRelocations) {
        GraqlGet.Unfiltered cityResidentsQuery = cityResidentsQuery(city, earliestDate);
        return tx().getOrderedAttribute(cityResidentsQuery, EMAIL, numRelocations);
    }

    @Override
    public List<String> getRelocationCityNames(World.City city) {
        GraqlGet.Unfiltered relocationCitiesQuery = Graql.match(
                Graql.var(CITY).isa(CITY).has(LOCATION_NAME, Graql.var("city-name")),
                Graql.var(CONTINENT).isa(CONTINENT).has(LOCATION_NAME, city.country().continent().name()),
                Graql.var("lh1").isa(LOCATION_HIERARCHY).rel(CITY).rel(CONTINENT),
                Graql.var("city-name").neq(city.name())
        ).get();
        return tx().getOrderedAttribute(relocationCitiesQuery, "city-name", null);
    }

    @Override
    public ActionResult insertRelocation(World.City city, LocalDateTime today, String email, String newCityName) {
        GraqlInsert relocatePersonQuery = Graql.match(
                Graql.var(PERSON).isa(PERSON).has(EMAIL, email),
                Graql.var("new-city").isa(CITY).has(LOCATION_NAME, newCityName),
                Graql.var("old-city").isa(CITY).has(LOCATION_NAME, city.name())
        ).insert(
                Graql.var(RELOCATION).isa(RELOCATION)
                        .rel(RELOCATION_PREVIOUS_LOCATION, "old-city")
                        .rel(RELOCATION_NEW_LOCATION, "new-city")
                        .rel(RELOCATION_RELOCATED_PERSON, PERSON)
                        .has(RELOCATION_DATE, today)
        );
        return Action.singleResult(tx().execute(relocatePersonQuery));
    }

    @Override
    public ActionResult resultsForTesting(ConceptMap answer) {
        return new ActionResult() {
            {
                put(RelocationAgentField.PERSON_EMAIL, tx().getOnlyAttributeOfThing(answer, PERSON, EMAIL));
                //put(RelocationAgentField.OLD_CITY_NAME, tx().getOnlyAttributeOfThing(answer, "old-city", LOCATION_NAME)); //TODO Can't be compared with Neo4j as Neo doesn't support ternary relations
                put(RelocationAgentField.NEW_CITY_NAME, tx().getOnlyAttributeOfThing(answer, "new-city", LOCATION_NAME));
                put(RelocationAgentField.RELOCATION_DATE, tx().getOnlyAttributeOfThing(answer, RELOCATION, RELOCATION_DATE));
            }
        };
    }
}