package grakn.simulation.db.neo4j.initialise;

import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.neo4j.agents.interaction.AgeUpdateAgent;
import grakn.simulation.db.neo4j.agents.interaction.MarriageAgent;
import grakn.simulation.db.neo4j.agents.interaction.ParentshipAgent;
import grakn.simulation.db.neo4j.agents.interaction.PersonBirthAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.COUNTRY;

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
        throw new NotImplementedException();
    }

    @Override
    protected CountryAgentRunner company() {
        throw new NotImplementedException();
    }

    @Override
    protected CityAgentRunner employment() {
        throw new NotImplementedException();
    }

    @Override
    protected ContinentAgentRunner product() {
        throw new NotImplementedException();
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
