package grakn.simulation.agents;

import grakn.client.GraknClient.Transaction;
import grakn.client.answer.ConceptMap;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class PersonBirthAgent implements CityAgent {

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {

        List<String> femaleForenames = context.getWorld().getFemaleForenames().map(World.FemaleForename::getValue).collect(toList());
        List<String> maleForenames = context.getWorld().getMaleForenames().map(World.MaleForename::getValue).collect(toList());
        List<String> surnames = context.getWorld().getSurnames().map(World.Surname::getValue).collect(toList());

        Transaction tx = context.getGraknSession().transaction().write();
        Random random = randomSource.startNewRandom();
        LocalDateTime dateToday = context.getLocalDateTime();

        int numBirths = 5;
        for (int i = 0; i < numBirths; i++) {
            String gender;
            String forename;
            String surname = surnames.get(random.nextInt(surnames.size()));

            boolean genderBool = random.nextBoolean();
            if (genderBool) {
                gender = "male";
                forename = maleForenames.get(random.nextInt(maleForenames.size()));
            } else {
                gender = "female";
                forename = femaleForenames.get(random.nextInt(femaleForenames.size()));
            }

            // Email is used as a key and needs to be unique, which requires a lot of information
            String email = forename + "."
                    + surname + "_"
                    + dateToday.toString() + "_"
                    + i + "_"
                    + context.getSimulationStep() + "_"
                    + city.getName() + "_"
                    + city.getCountry().getName() + "_"
                    + city.getCountry().getContinent().getName()
                    + "@gmail.com";

            GraqlInsert query =
                    Graql.match(
                            Graql.var("c").isa("city")
                                    .has("name", city.getName()))
                            .insert(Graql.var("p").isa("person")
                                            .has("email", email)
                                            .has("date-of-birth", dateToday)
                                            .has("gender", gender)
                                            .has("forename", forename)
                                            .has("surname", surname),
                                    Graql.var("b").isa("born-in")
                                            .rel("born-in_child", "p")
                                            .rel("born-in_place-of-birth", "c")
                            );

            List<ConceptMap> answers = tx.execute(query);
        }
        tx.commit();
    }
}