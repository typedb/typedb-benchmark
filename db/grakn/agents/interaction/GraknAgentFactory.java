package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.interaction.AgentFactory;
import grakn.simulation.db.common.agents.interaction.EmploymentAgent;
import grakn.simulation.db.grakn.context.GraknDriver;

public class GraknAgentFactory extends AgentFactory<GraknDriver> {
    @Override
    protected EmploymentAgent<GraknDriver> employment() {
        return new EmploymentAgent<GraknDriver>();
    }
}
