package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.interaction.AgeUpdateAgentBase;
import grakn.simulation.db.common.world.World;
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

public class AgeUpdateAgent extends GraknAgent<World.City> implements AgeUpdateAgentBase {

    @Override
    public void updateAgesOfAllPeople(LocalDateTime today, World.City city) {
        // Get all people born in a city
        HashMap<String, LocalDateTime> peopleAnswers;
        try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("getPeopleBornInCity"))) {
            peopleAnswers = getPeopleBornInCity(city);
        }
        newAction("updatePersonAge");
        // Update their ages
        peopleAnswers.forEach((personEmail, personDob) -> {
                    long age = ChronoUnit.YEARS.between(personDob, today);
                    try (ThreadTrace trace = traceOnThread(action())) {
                        updatePersonAge(personEmail, age);
                    }
                }
        );
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
        tx().execute(deleteImplicitQuery);

        GraqlInsert insertNewAgeQuery = Graql.match(
                person
                        .isa(PERSON)
                        .has(EMAIL, personEmail)
        ).insert(
                person
                        .has(AGE, newAge)
        );
        tx().execute(insertNewAgeQuery);
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
        newAction("getPeopleBornInCity");
        tx().execute(peopleQuery).forEach(personAnswer -> {
            LocalDateTime dob = (LocalDateTime) personAnswer.get(DATE_OF_BIRTH).asAttribute().value();
            String email = personAnswer.get(EMAIL).asAttribute().value().toString();
            peopleDobs.put(email, dob);
        });
        return peopleDobs;
    }

//    protected int checkCount() {
//        Statement city = Graql.var(CITY);
//        Statement person = Graql.var(PERSON);
//        Statement bornIn = Graql.var(BORN_IN);
//        Statement dobVar = Graql.var(DATE_OF_BIRTH);
//        Statement emailVar = Graql.var(EMAIL);
//        Statement age = Graql.var(AGE);
//
//        // This works as the diff matches the number of people born in the last run of PersonBirthAgent
//        GraqlGet.Aggregate countQuery = Graql.match(
//                city.isa(CITY)
//                        .has(LOCATION_NAME, city().toString()),
//                person.isa(PERSON)
//                        .has(EMAIL, emailVar)
//                        .has(DATE_OF_BIRTH, dobVar)
//                        .has(AGE, age),
//                bornIn.isa(BORN_IN)
//                        .rel(BORN_IN_CHILD, person)
//                        .rel(BORN_IN_PLACE_OF_BIRTH, city)
//        ).get().count();
//        log().query("checkCount", countQuery);
//        return tx().count(countQuery);
//    }
}
