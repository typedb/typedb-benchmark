package grakn.simulation.db.neo4j.initialise;

import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.neo4j.agents.interaction.AgeUpdateAgent;
import grakn.simulation.db.neo4j.agents.interaction.CompanyAgent;
import grakn.simulation.db.neo4j.agents.interaction.EmploymentAgent;
import grakn.simulation.db.neo4j.agents.interaction.FriendshipAgent;
import grakn.simulation.db.neo4j.agents.interaction.MarriageAgent;
import grakn.simulation.db.neo4j.agents.interaction.ParentshipAgent;
import grakn.simulation.db.neo4j.agents.interaction.PersonBirthAgent;
import grakn.simulation.db.neo4j.agents.interaction.ProductAgent;
import grakn.simulation.db.neo4j.agents.interaction.RelocationAgent;
import grakn.simulation.db.neo4j.agents.interaction.TransactionAgent;

import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.CITY;
import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.COUNTRY;

public class Neo4jAgentPicker extends AgentPicker {
    @Override
    protected CityAgentRunner marriage() {
        return new CityAgentRunner(MarriageAgent.class, CITY);
    }

    @Override
    protected CityAgentRunner personBirth() {
        return new CityAgentRunner(PersonBirthAgent.class, CITY);
    }

    @Override
    protected CityAgentRunner ageUpdate() {
        return new CityAgentRunner(AgeUpdateAgent.class, CITY);
    }

    @Override
    protected CityAgentRunner parentship() {
        return new CityAgentRunner(ParentshipAgent.class, CITY);
    }

    @Override
    protected CityAgentRunner relocation() {
        return new CityAgentRunner(RelocationAgent.class, CITY);
    }

    @Override
    protected CountryAgentRunner company() {
        return new CountryAgentRunner(CompanyAgent.class, CountryAgentRunner.SessionStrategy.COUNTRY);
    }

    @Override
    protected CityAgentRunner employment() {
        return new CityAgentRunner(EmploymentAgent.class, CITY);
    }

    @Override
    protected ContinentAgentRunner product() {
        return new ContinentAgentRunner(ProductAgent.class);
    }

    @Override
    protected ContinentAgentRunner transaction() {
        return new ContinentAgentRunner(TransactionAgent.class);
    }

    @Override
    protected CityAgentRunner friendship() {
        return new CityAgentRunner(FriendshipAgent.class, CITY);
    }
}
