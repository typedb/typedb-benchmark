package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CityAgent;

import java.util.HashMap;
import java.util.HashSet;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class PersonBirthAgentBase extends CityAgent {

    private int numBirths;

    protected enum PersonBirthAgentField implements ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }

    @Override
    public final AgentResult iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        numBirths = world().getScaleFactor();
        HashSet<HashMap<ComparableField, Object>> allFieldValues = new HashSet<>();

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
            HashMap<ComparableField, Object> fieldValues;
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertPerson"))) {
                fieldValues = insertPerson(email, gender, forename, surname);
            }
            allFieldValues.add(fieldValues);
        }
        commitTxWithTracing();
        return new AgentResult(allFieldValues);
    }

    protected abstract HashMap<ComparableField, Object> insertPerson(String email, String gender, String forename, String surname);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(numBirths, numBirths);
    }
}
