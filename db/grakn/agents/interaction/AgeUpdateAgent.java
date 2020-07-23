package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class AgeUpdateAgent extends grakn.simulation.db.common.agents.interaction.AgeUpdateAgent<GraknClientWrapper.Session, GraknClientWrapper.Transaction> {

    @Override
    protected void updateAgesOfAllPeople() {
        // Get all people born in a city
        Stream<ConceptMap> peopleAnswers = getPeopleBornInCity();
        // Update their ages
        log().message("updateAgesOfAllPeople");
        peopleAnswers.forEach(personAnswer -> {
                    LocalDateTime dob = (LocalDateTime) personAnswer.get("dob").asAttribute().value();
                    long age = ChronoUnit.YEARS.between(dob, today());
                    updatePersonAge(personAnswer.get("email").asAttribute().value().toString(), age);
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

    private Stream<ConceptMap> getPeopleBornInCity() {
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
        return tx().forGrakn().stream(peopleQuery).get();
    }
}
