package grakn.simulation;

import grakn.simulation.agents.*;

import java.util.Arrays;
import java.util.List;

public class AgentRunnerList {
    static AgentRunner[] AGENTS = {
            new CityAgentRunner(MarriageAgent.class),
            new CityAgentRunner(MarriageAgent.class),
            new CityAgentRunner(ParentshipAgent.class),
            new CityAgentRunner(RelocationAgent.class),
    };
}
