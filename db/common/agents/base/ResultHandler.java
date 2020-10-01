package grakn.simulation.db.common.agents.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ResultHandler {

    ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<ActionResult>>> agentResults;

    public void newResult(String agentName, String tracker, HashMap<String, ArrayList<ActionResult>> agentResult) {
        if (agentResult == null) {
            throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
        }
        ConcurrentHashMap<String, ArrayList<ActionResult>> agentEntry = agentResults.computeIfAbsent(agentName, x -> new ConcurrentHashMap<>());
        agentEntry.put(tracker, agentResult);
    }

    public ConcurrentHashMap<String, ArrayList<ActionResult>> getResultForAgent(String agentName) {
        return agentResults.get(agentName);
    }

    public void clean() {
        agentResults = new ConcurrentHashMap<>();
    }
}
