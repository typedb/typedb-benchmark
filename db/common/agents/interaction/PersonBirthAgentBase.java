package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DatabaseContext;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class PersonBirthAgentBase<CONTEXT extends DatabaseContext> extends CityAgent<CONTEXT> {

    private int numBirths;

    protected enum PersonBirthAgentField implements ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }

    @Override
    public final AgentResultSet iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        numBirths = world().getScaleFactor();
        AgentResultSet agentResultSet = new AgentResultSet();
        openTx();
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
                agentResultSet.add(insertPerson(email, gender, forename, surname));
            }
        }
        commitTx();
        return agentResultSet;
    }

    protected abstract AgentResult insertPerson(String email, String gender, String forename, String surname);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(numBirths, numBirths);
    }
}
