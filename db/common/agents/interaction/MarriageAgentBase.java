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
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        LocalDateTime dobOfAdults = iterationContext.today().minusYears(iterationContext.world().AGE_OF_ADULTHOOD);
        List<String> womenEmails;
        agent.newAction("getSingleWomen");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            womenEmails = getSingleWomen(city, dobOfAdults);
        }
        shuffle(womenEmails, agent.random());

        agent.newAction("getSingleMen");
        List<String> menEmails;
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            menEmails = getSingleMen(city, dobOfAdults);
        }
        shuffle(menEmails, agent.random());

        int numMarriages = iterationContext.world().getScaleFactor();

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {
            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = womenEmails.get(i);
                String husbandEmail = menEmails.get(i);
                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                agent.newAction("insertMarriage");
                try (ThreadTrace trace = traceOnThread(agent.action())) {
                    agentResultSet.add(insertMarriage(city, marriageIdentifier, wifeEmail, husbandEmail));
                }
            }
            agent.commitAction();
        } else {
            agent.closeAction();
        }
        return agentResultSet;
    }

    List<String> getSingleWomen(World.City city, LocalDateTime dobOfAdults);

    List<String> getSingleMen(World.City city, LocalDateTime dobOfAdults);

    List<String> getUnmarriedPeopleOfGender(World.City city, String gender, LocalDateTime dobOfAdults);

    AgentResult insertMarriage(World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);
}
