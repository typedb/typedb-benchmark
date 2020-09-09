package grakn.simulation.db.common.agents.base;

import java.util.concurrent.ConcurrentHashMap;

public class ResultHandler {

    ConcurrentHashMap<String, ConcurrentHashMap<String, AgentResultSet>> agentResults;

    public void newResult(String agentName, String tracker, AgentResultSet agentResult) {
        if (agentResult == null) {
            throw new NullPointerException(String.format("The result returned from a %s agent was null", agentName));
        }
        ConcurrentHashMap<String, AgentResultSet> agentEntry = agentResults.computeIfAbsent(agentName, x -> new ConcurrentHashMap<>());
        agentEntry.put(tracker, agentResult);
    }

    public ConcurrentHashMap<String, AgentResultSet> getResultForAgent(String agentName) {
        return agentResults.get(agentName);
    }

    public void clean() {
        agentResults = new ConcurrentHashMap<>();
    }
}
