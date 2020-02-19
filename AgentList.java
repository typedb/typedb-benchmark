package grakn.simulation;

import grakn.simulation.agents.*;

public class AgentList {
    static Agent[] AGENTS = {
        new MarriageAgent(),
        new PersonBirthAgent(),
        new ParentshipAgent(),
        new RelocationAgent(),
        new CompanyAgent(),
        new EmploymentAgent(),
    };
}
