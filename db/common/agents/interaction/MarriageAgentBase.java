package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static java.util.Collections.shuffle;

public interface MarriageAgentBase extends Agent.RegionalAgent<World.City> {

    enum MarriageAgentField implements DbOperationController.ComparableField {
        MARRIAGE_IDENTIFIER, WIFE_EMAIL, HUSBAND_EMAIL, CITY_NAME
    }

    @Override
    default void iterate(DbOperationController<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        ActionResultList agentResultSet = new ActionResultList();
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        LocalDateTime dobOfAdults = simulationContext.today().minusYears(simulationContext.world().AGE_OF_ADULTHOOD);
        List<String> womenEmails;
        agent.startDbOperation("getSingleWomen", tracker);
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            womenEmails = getSingleWomen(city, dobOfAdults);
        }
        shuffle(womenEmails, agent.random());

        agent.startDbOperation("getSingleMen", tracker);
        List<String> menEmails;
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            menEmails = getSingleMen(city, dobOfAdults);
        }
        shuffle(menEmails, agent.random());

        int numMarriages = simulationContext.world().getScaleFactor();

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {
            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = womenEmails.get(i);
                String husbandEmail = menEmails.get(i);
                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                agent.startDbOperation("insertMarriage", tracker);
                try (ThreadTrace trace = traceOnThread(agent.action())) {
                    agentResultSet.add(insertMarriage(city, marriageIdentifier, wifeEmail, husbandEmail));
                }
            }
            agent.saveDbOperation();
        } else {
            agent.closeDbOperation();
        }
        return agentResultSet;
    }

    List<String> getSingleWomen(World.City city, LocalDateTime dobOfAdults);

    List<String> getSingleMen(World.City city, LocalDateTime dobOfAdults);

    List<String> getUnmarriedPeopleOfGender(World.City city, String gender, LocalDateTime dobOfAdults);

    ActionResult insertMarriage(World.City city, int marriageIdentifier, String wifeEmail, String husbandEmail);
}
