package grakn.simulation;

import grakn.simulation.agents.AgeUpdateAgent;
import grakn.simulation.agents.CompanyAgent;
import grakn.simulation.agents.EmploymentAgent;
import grakn.simulation.agents.FriendshipAgent;
import grakn.simulation.agents.MarriageAgent;
import grakn.simulation.agents.ParentshipAgent;
import grakn.simulation.agents.PersonBirthAgent;
import grakn.simulation.agents.ProductAgent;
import grakn.simulation.agents.RelocationAgent;
import grakn.simulation.agents.TransactionAgent;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.agents.common.CityAgentRunner;
import grakn.simulation.agents.common.ContinentAgentRunner;
import grakn.simulation.agents.common.CountryAgentRunner;

import java.util.HashMap;

public class AgentRunners {
    static HashMap<String, AgentRunner<?>> AGENTS;
    static {
        AGENTS = new HashMap<>();
        AGENTS.put("MarriageAgent", new CityAgentRunner(MarriageAgent.class));
        AGENTS.put("PersonBirthAgent", new CityAgentRunner(PersonBirthAgent.class));
        AGENTS.put("AgeUpdateAgent", new CityAgentRunner(AgeUpdateAgent.class));
        AGENTS.put("ParentshipAgent", new CityAgentRunner(ParentshipAgent.class));
        AGENTS.put("RelocationAgent", new CityAgentRunner(RelocationAgent.class));
        AGENTS.put("CompanyAgent", new CountryAgentRunner(CompanyAgent.class));
        AGENTS.put("EmploymentAgent", new CityAgentRunner(EmploymentAgent.class));
        AGENTS.put("ProductAgent", new ContinentAgentRunner(ProductAgent.class));
        AGENTS.put("TransactionAgent", new CountryAgentRunner(TransactionAgent.class));
        AGENTS.put("FriendshipAgent", new CityAgentRunner(FriendshipAgent.class));
    }
}