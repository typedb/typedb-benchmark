package grakn.simulation.agents;

import grakn.client.answer.ConceptMap;
import grakn.simulation.agents.common.CityAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class AgeUpdateAgent extends CityAgent {

    @Override
    public void iterate() {
//        Get all people born in a city
        Stream<ConceptMap> peopleAnswers = getPeopleBornInCity();
//        Update their ages
        peopleAnswers.forEach(personAnswer -> {
                    LocalDateTime dob = (LocalDateTime) personAnswer.get("dob").asAttribute().value();
                    long age = ChronoUnit.YEARS.between(dob, today());
                    updatePersonAge(personAnswer.get("email").asAttribute().value().toString(), age);
                }
        );

        tx().commit();
    }

    private void updatePersonAge(String personEmail, long newAge) {
        GraqlDelete.Unfiltered deleteImplicitQuery = Graql.match(
                Graql.var("p").isa("person")
                        .has("email", personEmail)
                        .has("age", Graql.var("age"), Graql.var("r"))
        ).delete("r");

        log().query("deleteImplicitQuery", deleteImplicitQuery);
        tx().execute(deleteImplicitQuery);

        GraqlInsert insertNewAgeQuery = Graql.match(
                Graql.var("p").isa("person")
                        .has("email", personEmail)
        ).insert(
                Graql.var("p")
                        .has("age", newAge)
        );

        log().query("insertNewAgeQuery", insertNewAgeQuery);
        tx().execute(insertNewAgeQuery);
    }

    private Stream<ConceptMap> getPeopleBornInCity() {
        GraqlGet.Sorted peopleQuery = getPeopleBornInCityQuery();
        log().query("getPeopleBornInCity", peopleQuery);
        return tx().stream(peopleQuery).get();
    }

    private GraqlGet.Sorted getPeopleBornInCityQuery() {
        return Graql.match(
                Graql.var("c").isa("city")
                        .has("location-name", city().toString()),
                Graql.var("p").isa("person")
                        .has("email", Graql.var("email"))
                        .has("date-of-birth", Graql.var("dob")),
                Graql.var("b").isa("born-in")
                        .rel("born-in_child", "p")
                        .rel("born-in_place-of-birth", "c")
        ).get().sort("email");
    }
}
