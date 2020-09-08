package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.grakn.driver.GraknClientWrapper.Session.Transaction;
import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.AGE;

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent {

    @Override
    protected AgentResult updateAgesOfAllPeople() {
        // Get all people born in a city
        HashMap<String, LocalDateTime> peopleAnswers;
        try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("getPeopleBornInCity"))) {
            peopleAnswers = getPeopleBornInCity();
        }
        // Update their ages
        peopleAnswers.forEach((personEmail, personDob) -> {
                    long age = ChronoUnit.YEARS.between(personDob, today());
                    try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("updatePersonAge"))) {
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

        log().query("deleteImplicitQuery", deleteImplicitQuery);
        tx().forGrakn().execute(deleteImplicitQuery).get();

        GraqlInsert insertNewAgeQuery = Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                person
                        .has(AGE, newAge)
        );

        log().query("insertNewAgeQuery", insertNewAgeQuery);
        tx().forGrakn().execute(insertNewAgeQuery).get();
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity() {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);
        Statement emailVar = Graql.var(EMAIL);

        GraqlGet.Sorted peopleQuery = Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, city().toString()),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar),
                bornIn.isa(BORN_IN)
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
        ).get().sort(EMAIL);
        log().query("getPeopleBornInCity", peopleQuery);

        HashMap<String, LocalDateTime> peopleDobs = new HashMap<>();

        tx().forGrakn().execute(peopleQuery).get().forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asAttribute().value();
            String email = personAnswer.get(EMAIL).asAttribute().value().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }

    @Override
    protected int checkCount() {
        Statement city = Graql.var(CITY);
        Statement person = Graql.var(PERSON);
        Statement bornIn = Graql.var(BORN_IN);
        Statement dobVar = Graql.var(DATE_OF_BIRTH);
        Statement emailVar = Graql.var(EMAIL);
        Statement age = Graql.var(AGE);

        // This works as the diff matches the number of people born in the last run of PersonBirthAgent
        GraqlGet.Aggregate countQuery = Graql.match(
                city.isa(CITY)
                        .has(LOCATION_NAME, city().toString()),
                person.isa(PERSON)
                        .has(EMAIL, emailVar)
                        .has(DATE_OF_BIRTH, dobVar)
                        .has(AGE, age),
                bornIn.isa(BORN_IN)
                        .rel(BORN_IN_CHILD, person)
                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
        ).get().count();
        log().query("checkCount", countQuery);
        return ((Transaction) tx()).count(countQuery);
    }
}
