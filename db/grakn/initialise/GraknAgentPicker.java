package grakn.simulation.db.grakn.initialise;

import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.grakn.agents.interaction.AgeUpdateAgent;
import grakn.simulation.db.grakn.agents.interaction.CompanyAgent;
import grakn.simulation.db.grakn.agents.interaction.EmploymentAgent;
import grakn.simulation.db.grakn.agents.interaction.FriendshipAgent;
import grakn.simulation.db.grakn.agents.interaction.MarriageAgent;
import grakn.simulation.db.grakn.agents.interaction.ParentshipAgent;
import grakn.simulation.db.grakn.agents.interaction.PersonBirthAgent;
import grakn.simulation.db.grakn.agents.interaction.ProductAgent;
import grakn.simulation.db.grakn.agents.interaction.RelocationAgent;
import grakn.simulation.db.grakn.agents.interaction.TransactionAgent;

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
