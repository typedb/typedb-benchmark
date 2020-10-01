package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface ParentshipAgentBase extends InteractionAgent<World.City> {

    enum SpouseType {
        WIFE, HUSBAND
    }

    enum ParentshipField implements Agent.ComparableField {
        HUSBAND_EMAIL, WIFE_EMAIL, CHILD_EMAIL
    }

    @Override
    default void iterate(Agent<World.City, ?> agent, World.City city, SimulationContext simulationContext) {
        // Query for married couples in the city who are not already in a parentship relation together
        List<String> childrenEmails;
        agent.startDbOperation("getChildrenEmailsBorn");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            childrenEmails = getChildrenEmailsBorn(city, simulationContext.today());
        }
        List<HashMap<SpouseType, String>> marriageEmails;
        agent.startDbOperation("getMarriageEmails");
        try (ThreadTrace trace = traceOnThread(agent.action())) {
            marriageEmails = getMarriageEmails(city);
        }

        ActionResultList agentResultSet = new ActionResultList();
        if (marriageEmails.size() > 0 && childrenEmails.size() > 0) {
            LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriageEmails.size());

            for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                Integer marriageIndex = childrenForMarriage.getKey();
                List<Integer> children = childrenForMarriage.getValue();

                HashMap<SpouseType, String> marriage = marriageEmails.get(marriageIndex);

                for (Integer childIndex : children) {
                    String childEmail = childrenEmails.get(childIndex);
                    agent.startDbOperation("insertParentShip");
                    try (ThreadTrace trace = traceOnThread(agent.action())) {
                        agentResultSet.add(insertParentShip(marriage, childEmail));
                    }
                }
            }
            agent.saveDbOperation();
        } else {
            agent.closeDbOperation();
        }
        return agentResultSet;
    }

    List<HashMap<SpouseType, String>> getMarriageEmails(World.City city);

    List<String> getChildrenEmailsBorn(World.City city, LocalDateTime today);

    ActionResult insertParentShip(HashMap<SpouseType, String> marriage, String childEmail);
}
