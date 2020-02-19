package grakn.simulation;

import grakn.simulation.agents.*;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.agents.common.CityAgentRunner;

public class AgentRunnerList {
    static AgentRunner[] AGENTS = {
            new CityAgentRunner(MarriageAgent.class),
            new CityAgentRunner(MarriageAgent.class),
            new CityAgentRunner(ParentshipAgent.class),
            new CityAgentRunner(RelocationAgent.class),
    };
}
