package grakn.simulation.db.grakn.agents.interaction;

import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static com.google.common.net.HttpHeaders.AGE;
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

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent {

    @Override
    protected void updateAgesOfAllPeople() {
        // Get all people born in a city
        HashMap<String, LocalDateTime> peopleAnswers;
        try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("getPeopleBornInCity"))) {
            peopleAnswers = getPeopleBornInCity();
        }
        // Update their ages
        log().message("updateAgesOfAllPeople");
        peopleAnswers.forEach((personEmail, personDob) -> {
                    long age = ChronoUnit.YEARS.between(personDob, today());
                    try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("updatePersonAge"))) {
                        updatePersonAge(personEmail, age);
                    }
                }
        );
    }

    private void updatePersonAge(String personEmail, long newAge) {
        GraqlDelete deleteImplicitQuery = Graql.match(
                Graql.var(PERSON).isa(PERSON)
                        .has(EMAIL, personEmail)
                        .has(AGE, Graql.var(AGE))
        ).delete(Graql.var(PERSON).has(AGE, Graql.var(AGE)));

        log().query("deleteImplicitQuery", deleteImplicitQuery);
        tx().forGrakn().execute(deleteImplicitQuery);

        GraqlInsert insertNewAgeQuery = Graql.match(
                Graql.var(PERSON).isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                Graql.var(PERSON)
                        .has(AGE, newAge)
        );

        log().query("insertNewAgeQuery", insertNewAgeQuery);
        tx().forGrakn().execute(insertNewAgeQuery);
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity() {
        GraqlGet.Sorted peopleQuery = Graql.match(
                Graql.var(CITY).isa(CITY)
                        .has(LOCATION_NAME, city().toString()),
                Graql.var(PERSON).isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL))
                        .has(DATE_OF_BIRTH, Graql.var(DATE_OF_BIRTH)),
                Graql.var(BORN_IN).isa(BORN_IN)
                        .rel(BORN_IN_CHILD, PERSON)
                        .rel(BORN_IN_PLACE_OF_BIRTH, CITY)
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
        if(simulationStep() == 0) {
            GraqlGet.Aggregate countQuery = Graql.match(
                    Graql.var(PERSON).isa(PERSON)
                            .has(EMAIL, Graql.var(EMAIL))
                            .has(AGE, Graql.var(AGE))
            ).get().count();
            return tx().forGrakn().execute(countQuery).get().get(0).number().intValue();
        } else {
            return 0; // TODO Actually return the number of people born in the last iteration
        }
    }
}
