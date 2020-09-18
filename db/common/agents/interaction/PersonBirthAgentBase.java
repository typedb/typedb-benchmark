package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.agents.utils.CheckMethod;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface PersonBirthAgentBase extends InteractionAgent<World.City> {

    enum PersonBirthAgentField implements Agent.ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        int numBirths = iterationContext.world().getScaleFactor();
        AgentResultSet agentResultSet = new AgentResultSet();
        agent.startAction();
        for (int i = 0; i < numBirths; i++) {
            String gender;
            String forename;
            String surname = agent.pickOne(iterationContext.world().getSurnames());

            boolean genderBool = agent.random().nextBoolean();
            if (genderBool) {
                gender = "male";
                forename = agent.pickOne(iterationContext.world().getMaleForenames());
            } else {
                gender = "female";
                forename = agent.pickOne(iterationContext.world().getFemaleForenames());
            }

            // Email is used as a key and needs to be unique, which requires a lot of information
            String email = forename + "."
                    + surname + "_"
                    + iterationContext.today().toString() + "_"
                    + i + "_"
                    + iterationContext.simulationStep() + "_"
                    + city + "_"
                    + city.country() + "_"
                    + city.country().continent()
                    + "@gmail.com";
            String scope = "insertPerson";
            try (ThreadTrace trace = traceOnThread(CheckMethod.checkMethodExists(this, scope))) {
                agentResultSet.add(insertPerson(scope, city, iterationContext.today(), email, gender, forename, surname));
            }
        }
        agent.commitAction();
        return agentResultSet;
    }

    AgentResult insertPerson(String scope, World.City city, LocalDateTime today, String email, String gender, String forename, String surname);
}
