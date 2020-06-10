package grakn.simulation.agents;

import grakn.simulation.agents.common.CityAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;

public class PersonBirthAgent extends CityAgent {

    @Override
    public void iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        int numBirths = world().getScaleFactor();
        for (int i = 0; i < numBirths; i++) {
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
                + city() + "_"
                + city().country() + "_"
                + city().country().continent()
                + "@gmail.com";

        GraqlInsert query =
                Graql.match(
                        Graql.var("c").isa("city")
                                .has("location-name", city().toString()))
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
        log().query("insertPerson", query);
        tx().execute(query);
    }
}