package grakn.simulation.agents;

import grakn.simulation.agents.common.CityAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

public class PersonBirthAgent extends CityAgent {

    private static final World.WorldLogWrapper LOG = World.log(PersonBirthAgent.class);
    private static final int NUM_BIRTHS = 5;

    @Override
    public void iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        for (int i = 0; i < NUM_BIRTHS; i++) {
            insertPerson(i);
        }
        tx().commit();
    }

    private void insertPerson(int i) {
        String gender;
        String forename;
        String surname = pickOne(world().getSurnames());

        boolean genderBool = random().nextBoolean();
        if (genderBool) {
            gender = "male";
            forename = pickOne(world().getMaleForenames());
        } else {
            gender = "female";
            forename = pickOne(world().getFemaleForenames());
        }

        // Email is used as a key and needs to be unique, which requires a lot of information
        String email = forename + "."
                + surname + "_"
                + today().toString() + "_"
                + i + "_"
                + simulationStep() + "_"
                + city().getName() + "_"
                + city().getCountry().getName() + "_"
                + city().getCountry().getContinent().getName()
                + "@gmail.com";

        GraqlInsert query =
                Graql.match(
                        Graql.var("c").isa("city")
                                .has("name", city().getName()))
                        .insert(Graql.var("p").isa("person")
                                        .has("email", email)
                                        .has("date-of-birth", today())
                                        .has("gender", gender)
                                        .has("forename", forename)
                                        .has("surname", surname),
                                Graql.var("b").isa("born-in")
                                        .rel("born-in_child", "p")
                                        .rel("born-in_place-of-birth", "c")
                        );
        LOG.query(city(), "insertPerson", query);
        tx().execute(query);
    }
}