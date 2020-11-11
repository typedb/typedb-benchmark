package grakn.simulation.db.grakn.action.write;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.common.world.World;
import grakn.simulation.db.grakn.driver.GraknOperation;
import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.grakn.schema.Schema.AGE;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;

public class GraknUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<GraknOperation> {
    public GraknUpdateAgesOfPeopleInCityAction(GraknOperation dbOperation, LocalDateTime today, World.City city) {
        super(dbOperation, today, city);
    }

    @Override
    public Integer run() {
        // Get all people born in a city
        HashMap<String, LocalDateTime> peopleAnswers;
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("getPeopleBornInCity")) {
            peopleAnswers = getPeopleBornInCity(city);
        }
        // Update their ages
        peopleAnswers.forEach((personEmail, personDob) -> {
                    long age = ChronoUnit.YEARS.between(personDob, today);
                    try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("updatePersonAge")) {
                        updatePersonAge(personEmail, age);
                    }
                }
        );
        return null;
    }

    public static GraqlGet.Sorted getPeopleBornInCityQuery(String worldCityName) {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);
        Statement emailVar = Graql.var(EMAIL);

        return Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, worldCityName),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar),
                bornIn.isa(BORN_IN)
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
        ).get().sort(EMAIL);
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity(World.City worldCity) {
        GraqlGet.Sorted peopleQuery = getPeopleBornInCityQuery(worldCity.name());

        HashMap<String, LocalDateTime> peopleDobs = new HashMap<>();
        dbOperation.execute(peopleQuery).forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asAttribute().value();
            String email = personAnswer.get(EMAIL).asAttribute().value().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }

    private void updatePersonAge(String personEmail, long newAge) {
        dbOperation.execute(deleteHasQuery(personEmail));
        dbOperation.execute(insertNewAgeQuery(personEmail, newAge));
    }

    public static GraqlInsert insertNewAgeQuery(String personEmail, long newAge) {
        Statement person = Graql.var(PERSON);
        return Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                person
                        .has(AGE, newAge)
        );
    }

    public static GraqlDelete deleteHasQuery(String personEmail) {
        Statement person = Graql.var(PERSON);
        Statement age = Graql.var(AGE);
        return Graql.match(
                    person
                            .isa(PERSON)
                            .has(EMAIL, personEmail)
                            .has(AGE, age)
            ).delete(
                    person
                            .has(AGE, age
                            )
            );
    }
}
