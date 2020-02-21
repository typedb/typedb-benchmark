package grakn.simulation;

import grakn.simulation.agents.*;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.agents.common.CityAgentRunner;
import grakn.simulation.agents.common.CountryAgentRunner;

public class AgentRunnerList {
    static AgentRunner[] AGENTS = {
            new CityAgentRunner(MarriageAgent.class),
            new CityAgentRunner(PersonBirthAgent.class),
            new CityAgentRunner(ParentshipAgent.class),
            new CityAgentRunner(RelocationAgent.class),
            new CountryAgentRunner(CompanyAgent.class),
            new CityAgentRunner(EmploymentAgent.class)
    };
}
