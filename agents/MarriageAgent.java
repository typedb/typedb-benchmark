package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Transaction;
import grakn.client.answer.ConceptMap;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MarriageAgent implements CityAgent {

    private static final int NUM_MARRIAGES = 5;

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        city.log("-- Marriage Agent --");
        String sessionKey = city.getCountry().getContinent().getName();
        GraknClient.Session session = context.getIterationGraknSessionFor(sessionKey);

        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        try ( Transaction tx = session.transaction().write()) {

            GraqlGet.Unfiltered singleWomenQuery = getSinglePeople(context, city, "female", "marriage_wife");
            city.logQuery(singleWomenQuery);
            List<String> womenAnswers = tx.execute(singleWomenQuery).stream().map(a -> a.get("email").asAttribute().value().toString()).sorted().collect(Collectors.toList());

            GraqlGet.Unfiltered singleMenQuery = getSinglePeople(context, city, "male", "marriage_husband");
            city.logQuery(singleMenQuery);
            List<String> menAnswers = tx.execute(singleMenQuery).stream().map(a -> a.get("email").asAttribute().value().toString()).sorted().collect(Collectors.toList());

            Random rnd = randomSource.startNewRandom();

            Collections.shuffle(womenAnswers, rnd);
            Collections.shuffle(menAnswers, rnd);

            int numMarriagesPossible = Math.min(NUM_MARRIAGES, Math.min(womenAnswers.size(), menAnswers.size()));

            if (numMarriagesPossible > 0) {

                for (int i = 0; i < numMarriagesPossible; i++) {
                    String wifeEmail = womenAnswers.get(i);
                    String husbandEmail = menAnswers.get(i);

                    int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                    GraqlInsert marriageQuery = Graql.match(
                            Graql.var("husband").isa("person").has("email", husbandEmail),
                            Graql.var("wife").isa("person").has("email", wifeEmail),
                            Graql.var("city").isa("city").has("name", city.getName())
                    ).insert(
                            Graql.var("m").isa("marriage")
                                    .rel("marriage_husband", "husband")
                                    .rel("marriage_wife", "wife")
                                    .has("marriage-id", marriageIdentifier),
                            Graql.var().isa("locates").rel("locates_located", Graql.var("m")).rel("locates_location", Graql.var("city"))
                    );
                    city.logQuery(marriageQuery);
                    List<ConceptMap> answers = tx.execute(marriageQuery);
                }
                tx.commit();
            }
        }
    }

    private GraqlGet.Unfiltered getSinglePeople(AgentContext context, World.City city, String gender, String marriageRole) {
        Statement personVar = Graql.var("p");
        Statement cityVar = Graql.var("city");

        LocalDateTime dobOfAdults = context.getLocalDateTime().minusYears(World.AGE_OF_ADULTHOOD);

        return Graql.match(
                personVar.isa("person").has("gender", gender).has("email", Graql.var("email")).has("date-of-birth", Graql.var("dob")),
                Graql.var("dob").lte(dobOfAdults),
                Graql.not(Graql.var("m").isa("marriage").rel(marriageRole, personVar)),
                Graql.var("r").isa("residency").rel("residency_resident", personVar).rel("residency_location", cityVar),
                Graql.not(Graql.var("r").has("end-date", Graql.var("ed"))),
                cityVar.isa("city").has("name", city.getName())
        ).get("email");
    }
}
