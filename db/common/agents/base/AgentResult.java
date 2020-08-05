package grakn.simulation.db.common.agents.base;

import grakn.simulation.db.common.agents.interaction.PersonBirthAgentBase;

import java.util.Collection;
import java.util.HashMap;

public class AgentResult {
    private final Collection<HashMap<PersonBirthAgentBase.Field, Object>> allFieldValues;

    public AgentResult(Collection<HashMap<PersonBirthAgentBase.Field, Object>> allFieldValues) {
        this.allFieldValues = allFieldValues;
    }

    public Collection<HashMap<PersonBirthAgentBase.Field, Object>> getAllFieldValues() {
        return allFieldValues;
    }
}
