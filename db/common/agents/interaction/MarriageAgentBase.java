package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static java.util.Collections.shuffle;

public interface MarriageAgentBase extends InteractionAgent<World.City> {

    enum MarriageAgentField implements Agent.ComparableField {
        MARRIAGE_IDENTIFIER, WIFE_EMAIL, HUSBAND_EMAIL, CITY_NAME
    }

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        AgentResultSet agentResultSet = new AgentResultSet();
        agent.log().message(agent.tracker(), "MarriageAgent", String.format("Simulation step %d", iterationContext.simulationStep()));
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<String> womenEmails;
        agent.startAction();

        LocalDateTime dobOfAdults = iterationContext.today().minusYears(iterationContext.world().AGE_OF_ADULTHOOD);

        String scope1 = "getSingleWomen";
        try (ThreadTrace trace = traceOnThread(scope1)) {
            womenEmails = getUnmarriedPeopleOfGender(scope1, city, "female", dobOfAdults);
        }
        shuffle(womenEmails, agent.random());

        String scope2 = "getSingleMen";
        List<String> menEmails;
        try (ThreadTrace trace = traceOnThread(scope2)) {
            menEmails = getUnmarriedPeopleOfGender(scope2, city, "male", dobOfAdults);
        }
        shuffle(menEmails, agent.random());

        int numMarriages = iterationContext.world().getScaleFactor();

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {
            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = womenEmails.get(i);
                String husbandEmail = menEmails.get(i);
                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                String scope = "insertMarriage";
                try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace(scope))) {
                    agentResultSet.add(insertMarriage(scope, city, marriageIdentifier, wifeEmail, husbandEmail));
                }
            }
            agent.commitAction();
        } else {
            agent.stopAction();
        }
        return agentResultSet;
    }

    List<String> getUnmarriedPeopleOfGender(String scope, World.City city, String gender, LocalDateTime dobOfAdults);

    AgentResult insertMarriage(String scope, World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);
}
