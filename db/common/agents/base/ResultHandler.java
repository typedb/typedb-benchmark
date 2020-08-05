package grakn.simulation.db.common.agents.base;

import java.util.HashMap;

public class ResultHandler {

    HashMap<String, AgentResult> agentResults;

    public void newResult(String agentName, AgentResult agentResult) {
        agentResults.put(agentName, agentResult);
    }

    public AgentResult getResultForAgent(String agentName) {
        return agentResults.get(agentName);
    }

    public void clean() {
        agentResults = new HashMap<>();
    }
}
