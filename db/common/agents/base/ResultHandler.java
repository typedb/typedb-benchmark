package grakn.simulation.db.common.agents.base;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ResultHandler {

    HashMap<String, ConcurrentHashMap<String, AgentResult>> agentResults;

    public void newResult(String agentName, String tracker, AgentResult agentResult) {
        if (agentResult == null) {
            throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
        }
        ConcurrentHashMap<String, AgentResult> agentEntry = agentResults.computeIfAbsent(agentName, x -> new ConcurrentHashMap<>());
        agentEntry.put(tracker, agentResult);
    }

    public ConcurrentHashMap<String, AgentResult> getResultForAgent(String agentName) {
        return agentResults.get(agentName);
    }

    public void clean() {
        agentResults = new HashMap<>();
    }
}
