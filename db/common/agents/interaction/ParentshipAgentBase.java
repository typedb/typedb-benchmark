package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
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
    default AgentResultSet iterate(Agent<World.City, ?> agent, World.City city, IterationContext iterationContext) {
        // Query for married couples in the city who are not already in a parentship relation together
        List<String> childrenEmails;
        agent.startAction();
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getChildrenEmailsBorn"))) {
            childrenEmails = getChildrenEmailsBorn(city, iterationContext.today());
        }
        List<HashMap<Email, String>> marriageEmails;
        try (ThreadTrace trace = traceOnThread(agent.registerMethodTrace("getMarriageEmails"))) {
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
                try (ThreadTrace trace = traceOnThread(agent.checkMethodTrace("insertParentShip"))) {
                    insertParentShip(marriage, childEmails);
                }
            }
            agent.commitAction();
        } else {
            agent.stopAction();
        }
        return null;
    }

    List<HashMap<Email, String>> getMarriageEmails(World.City city);

    List<String> getChildrenEmailsBorn(World.City city, LocalDateTime dateToday);

    void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails);

}
