package grakn.simulation.db.common.agents.base;

import java.util.Collection;
import java.util.HashMap;

public class AgentResult {
    private final Collection<HashMap<Agent.ComparableField, Object>> allFieldValues;

    public AgentResult(Collection<HashMap<Agent.ComparableField, Object>> allFieldValues) {
        this.allFieldValues = allFieldValues;
    }

    public Collection<HashMap<Agent.ComparableField, Object>> getAllFieldValues() {
        return allFieldValues;
    }
}
