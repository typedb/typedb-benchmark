package grakn.simulation.db.neo4j.initialise;

import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.neo4j.agents.interaction.AgeUpdateAgent;
import grakn.simulation.db.neo4j.agents.interaction.CompanyAgent;
import grakn.simulation.db.neo4j.agents.interaction.EmploymentAgent;
import grakn.simulation.db.neo4j.agents.interaction.MarriageAgent;
import grakn.simulation.db.neo4j.agents.interaction.ParentshipAgent;
import grakn.simulation.db.neo4j.agents.interaction.PersonBirthAgent;
import grakn.simulation.db.neo4j.agents.interaction.ProductAgent;
import grakn.simulation.db.neo4j.agents.interaction.RelocationAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.COUNTRY;
import static grakn.simulation.db.common.agents.world.CountryAgentRunner.SessionStrategy.CONTINENT;

public class Neo4jAgentPicker extends AgentPicker {
    @Override
    protected CityAgentRunner marriage() {
        return new CityAgentRunner(MarriageAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner personBirth() {
        return new CityAgentRunner(PersonBirthAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner ageUpdate() {
        return new CityAgentRunner(AgeUpdateAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner parentship() {
        return new CityAgentRunner(ParentshipAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner relocation() {
        return new CityAgentRunner(RelocationAgent.class, COUNTRY);
    }

    @Override
    protected CountryAgentRunner company() {
        return new CountryAgentRunner(CompanyAgent.class, CountryAgentRunner.SessionStrategy.COUNTRY);
    }

    @Override
    protected CityAgentRunner employment() {
        return new CityAgentRunner(EmploymentAgent.class, COUNTRY);
    }

    @Override
    protected ContinentAgentRunner product() {
        return new ContinentAgentRunner(ProductAgent.class);
    }

    @Override
    protected CountryAgentRunner transaction() {
        throw new NotImplementedException();
    }

    @Override
    protected CityAgentRunner friendship() {
        throw new NotImplementedException();
    }
}
