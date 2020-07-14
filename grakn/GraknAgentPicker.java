package grakn.simulation.grakn;

import grakn.simulation.agents.base.AgentPicker;
import grakn.simulation.agents.world.CityAgentRunner;
import grakn.simulation.agents.world.ContinentAgentRunner;
import grakn.simulation.agents.world.CountryAgentRunner;
import grakn.simulation.grakn.agents.AgeUpdateAgent;
import grakn.simulation.grakn.agents.CompanyAgent;
import grakn.simulation.grakn.agents.EmploymentAgent;
import grakn.simulation.grakn.agents.FriendshipAgent;
import grakn.simulation.grakn.agents.MarriageAgent;
import grakn.simulation.grakn.agents.ParentshipAgent;
import grakn.simulation.grakn.agents.PersonBirthAgent;
import grakn.simulation.grakn.agents.ProductAgent;
import grakn.simulation.grakn.agents.RelocationAgent;
import grakn.simulation.grakn.agents.TransactionAgent;

public class GraknAgentPicker extends AgentPicker {

    @Override
    protected CityAgentRunner marriage() {
        return new CityAgentRunner(MarriageAgent.class);
    }

    @Override
    protected CityAgentRunner personBirth() {
        return new CityAgentRunner(PersonBirthAgent.class);
    }

    @Override
    protected CityAgentRunner ageUpdate() {
        return new CityAgentRunner(AgeUpdateAgent.class);
    }

    @Override
    protected CityAgentRunner parentship() {
        return new CityAgentRunner(ParentshipAgent.class);
    }

    @Override
    protected CityAgentRunner relocation() {
        return new CityAgentRunner(RelocationAgent.class);
    }

    @Override
    protected CountryAgentRunner company() {
        return new CountryAgentRunner(CompanyAgent.class);
    }

    @Override
    protected CityAgentRunner employment() {
        return new CityAgentRunner(EmploymentAgent.class);
    }

    @Override
    protected ContinentAgentRunner product() {
        return new ContinentAgentRunner(ProductAgent.class);
    }

    @Override
    protected CountryAgentRunner transaction() {
        return new CountryAgentRunner(TransactionAgent.class);
    }

    @Override
    protected CityAgentRunner friendship() {
        return new CityAgentRunner(FriendshipAgent.class);
    }

}
