package grakn.simulation.db.common.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.world.CityAgent;
import graql.lang.query.GraqlGet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public abstract class AgeUpdateAgent extends CityAgent {

    @Override
    public final void iterate() {
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

    protected abstract void updatePersonAge(String personEmail, long newAge);

    protected abstract Stream<ConceptMap> getPeopleBornInCity();

    protected abstract GraqlGet.Sorted getPeopleBornInCityQuery();
}
