package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface ParentshipAgentBase extends InteractionAgent<World.City> {

    enum Email {
        WIFE, HUSBAND
    }

    @Override
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        // Query for married couples in the city who are not already in a parentship relation together
        List<String> childrenEmails;
        agent.newAction("getChildrenEmailsBorn");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            childrenEmails = getChildrenEmailsBorn(city, simulationContext.today());
        }
        List<HashMap<Email, String>> marriageEmails;
        agent.newAction("getMarriageEmails");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            marriageEmails = getMarriageEmails(city);
        }

        if (marriageEmails.size() > 0 && childrenEmails.size() > 0) {
            LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriageEmails.size());

            for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                Integer marriageIndex = childrenForMarriage.getKey();
                List<Integer> children = childrenForMarriage.getValue();

                HashMap<Email, String> marriage = marriageEmails.get(marriageIndex);

                List<String> childEmails = new ArrayList<>();
                for (Integer childIndex : children) {
                    childEmails.add(childrenEmails.get(childIndex));
                }
                agent.newAction("insertParentShip");
                try (ThreadTrace trace = traceOnThread(agent.action())) {
                    insertParentShip(marriage, childEmails);
                }
            }
            agent.commitAction();
        } else {
            agent.closeAction();
        }
        return null;
    }

    List<HashMap<Email, String>> getMarriageEmails(World.City city);

    List<String> getChildrenEmailsBorn(World.City city, LocalDateTime today);

    void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails);

}
