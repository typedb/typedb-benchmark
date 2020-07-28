package grakn.simulation.db.grakn.agents.interaction;

import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

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
                Graql.var("p").isa("person")
                        .has("email", personEmail)
                        .has("age", Graql.var("age"))
        ).delete(Graql.var("p").has("age", Graql.var("age")));

        log().query("deleteImplicitQuery", deleteImplicitQuery);
        tx().forGrakn().execute(deleteImplicitQuery);

        GraqlInsert insertNewAgeQuery = Graql.match(
                Graql.var("p").isa("person")
                        .has("email", personEmail)
        ).insert(
                Graql.var("p")
                        .has("age", newAge)
        );

        log().query("insertNewAgeQuery", insertNewAgeQuery);
        tx().forGrakn().execute(insertNewAgeQuery);
    }

    private HashMap<String, LocalDateTime> getPeopleBornInCity() {
        GraqlGet.Sorted peopleQuery = Graql.match(
                Graql.var("c").isa("city")
                        .has("location-name", city().toString()),
                Graql.var("p").isa("person")
                        .has("email", Graql.var("email"))
                        .has("date-of-birth", Graql.var("dob")),
                Graql.var("b").isa("born-in")
                        .rel("born-in_child", "p")
                        .rel("born-in_place-of-birth", "c")
        ).get().sort("email");
        log().query("getPeopleBornInCity", peopleQuery);

        HashMap<String, LocalDateTime> peopleDobs = new HashMap<>();

        tx().forGrakn().execute(peopleQuery).get().forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get("dob").asAttribute().value();
            String email = personAnswer.get("email").asAttribute().value().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }

    @Override
    protected int checkCount() {
        GraqlGet.Aggregate countQuery = Graql.match(
                Graql.var("p").isa("person")
                        .has("email", Graql.var("personEmail"))
                        .has("age", Graql.var("age"))
        ).get().count();
        return tx().forGrakn().execute(countQuery).get().get(0).number().intValue();
    }
}
