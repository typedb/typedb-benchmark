package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Transaction;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class PersonBirthAgent implements CityAgent {

    private static final int NUM_BIRTHS = 5;

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        city.log("-- Person Birth Agent --");
        List<String> femaleForenames = context.getWorld().getFemaleForenames();
        List<String> maleForenames = context.getWorld().getMaleForenames();
        List<String> surnames = context.getWorld().getSurnames();
        Random random = randomSource.startNewRandom();

        String sessionKey = city.getCountry().getContinent().getName();
        GraknClient.Session session = context.getIterationGraknSessionFor(sessionKey);

        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        try ( Transaction tx = session.transaction().write()) {
            for (int i = 0; i < NUM_BIRTHS; i++) {
                insertPerson(tx, city, context, random, surnames, femaleForenames, maleForenames, i);
            }
            tx.commit();
        }
    }

    private void insertPerson(Transaction tx, World.City city, AgentContext context, Random random, List<String> surnames, List<String> femaleForenames, List<String> maleForenames, int i) {
        String gender;
        String forename;
        String surname = surnames.get(random.nextInt(surnames.size()));
        LocalDateTime dateToday = context.getLocalDateTime();

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
        city.logQuery(query);
        tx.execute(query);
    }
}