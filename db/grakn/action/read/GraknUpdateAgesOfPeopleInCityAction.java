package grakn.simulation.db.grakn.action.read;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.operation.TransactionDbOperationController;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.grakn.driver.GraknDbOperationController;
import grakn.simulation.db.grakn.driver.GraknTransaction;
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

public class GraknUpdateAgesOfPeopleInCityAction extends UpdateAgesOfPeopleInCityAction<GraknDbOperationController.TransactionalDbOperation> {
    public GraknUpdateAgesOfPeopleInCityAction(TransactionDbOperationController<GraknTransaction>.TransactionalDbOperation dbOperation, LocalDateTime today, World.City city) {
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

    private void updatePersonAge(String personEmail, long newAge) {
        Statement person = Graql.var(PERSON);
        Statement age = Graql.var(AGE);
        GraqlDelete deleteImplicitQuery = Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
                        .has(AGE, age)
        ).delete(
                person
                        .has(AGE, age
                        )
        );
        dbOperation.tx().execute(deleteImplicitQuery);

        GraqlInsert insertNewAgeQuery = Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                person
                        .has(AGE, newAge)
        );
        dbOperation.tx().execute(insertNewAgeQuery);
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity(World.City worldCity) {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);
        Statement emailVar = Graql.var(EMAIL);

        GraqlGet.Sorted peopleQuery = Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, worldCity.toString()),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar),
                bornIn.isa(BORN_IN)
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
        ).get().sort(EMAIL);

        HashMap<String, LocalDateTime> peopleDobs = new HashMap<>();
        dbOperation.tx().execute(peopleQuery).forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asAttribute().value();
            String email = personAnswer.get(EMAIL).asAttribute().value().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }
}
