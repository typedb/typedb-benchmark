package grakn.simulation.agents;

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

public class MarriageAgent implements CityAgent {

    private AgentContext context;
    public static int numMarriages = 5;

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {

        this.context = context;

        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        Transaction tx = context.getGraknSession().transaction().write();

        List<ConceptMap> womenAnswers = tx.execute(getSinglePeople(city, "female", "marriage_wife"));
        List<ConceptMap> menAnswers = tx.execute(getSinglePeople(city, "male", "marriage_husband"));

        Random rnd = randomSource.startNewRandom();

        Collections.shuffle(womenAnswers, rnd);
        Collections.shuffle(menAnswers, rnd);

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenAnswers.size(), menAnswers.size()));

        if (numMarriagesPossible > 0) {

            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = (String) womenAnswers.get(i).get("email").asAttribute().value();
                String husbandEmail = (String) menAnswers.get(i).get("email").asAttribute().value();

                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                GraqlInsert query = Graql.match(
                        Graql.var("husband").isa("person").has("email", husbandEmail),
                        Graql.var("wife").isa("person").has("email", wifeEmail)
                ).insert(
                        Graql.var("m").isa("marriage")
                                .rel("marriage_husband", "husband")
                                .rel("marriage_wife", "wife")
                                .has("identifier", marriageIdentifier)
                );
                List<ConceptMap> answers = tx.execute(query);
            }
            tx.commit();
        } else {
            tx.close();
        }
    }

    private GraqlGet.Unfiltered getSinglePeople(World.City city, String gender, String marriageRole) {
        Statement personVar = Graql.var("p");
        Statement cityVar = Graql.var("city");

        LocalDateTime dobOfAdults = context.getLocalDateTime().minusYears(World.ageOfAdulthood);

        return Graql.match(
                personVar.isa("person").has("gender", gender).has("email", Graql.var("email")).has("date-of-birth", Graql.var("dob")),
                Graql.var("dob").lte(dobOfAdults),
                Graql.not(Graql.var("m").isa("marriage").rel(marriageRole, personVar)),
                Graql.var("r").isa("residency").rel("residency_resident", personVar).rel("residency_location", cityVar),
                cityVar.isa("city").has("name", city.getName())
        ).get("email");
    }
}
