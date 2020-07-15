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

import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.CONTINENT;
import static grakn.simulation.db.common.agents.world.CountryAgentRunner.SessionStrategy.COUNTRY;

public class GraknAgentPicker extends AgentPicker {

    @Override
    protected CityAgentRunner marriage() {
        return new CityAgentRunner(MarriageAgent.class, CONTINENT);
    }

    @Override
    protected CityAgentRunner personBirth() {
        return new CityAgentRunner(PersonBirthAgent.class, CONTINENT);
    }

    @Override
    protected CityAgentRunner ageUpdate() {
        return new CityAgentRunner(AgeUpdateAgent.class, CONTINENT);
    }

    @Override
    protected CityAgentRunner parentship() {
        return new CityAgentRunner(ParentshipAgent.class, CONTINENT);
    }

    @Override
    protected CityAgentRunner relocation() {
        return new CityAgentRunner(RelocationAgent.class, CONTINENT);
    }

    @Override
    protected CountryAgentRunner company() {
        return new CountryAgentRunner(CompanyAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner employment() {
        return new CityAgentRunner(EmploymentAgent.class, CONTINENT);
    }

    @Override
    protected ContinentAgentRunner product() {
        return new ContinentAgentRunner(ProductAgent.class);
    }

    @Override
    protected CountryAgentRunner transaction() {
        return new CountryAgentRunner(TransactionAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner friendship() {
        return new CityAgentRunner(FriendshipAgent.class, CONTINENT);
    }

}
