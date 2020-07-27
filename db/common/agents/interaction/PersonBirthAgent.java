package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class PersonBirthAgent extends CityAgent {

    @Override
    public final void iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        int numBirths = world().getScaleFactor();
        for (int i = 0; i < numBirths; i++) {
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
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertPerson"))) {
                insertPerson(email, gender, forename, surname);
            }
        }
        tx().commitWithTracing();
    }

    protected abstract void insertPerson(String email, String gender, String forename, String surname);
}
