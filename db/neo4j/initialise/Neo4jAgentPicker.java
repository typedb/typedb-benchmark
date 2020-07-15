package grakn.simulation.db.neo4j.initialise;

import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.neo4j.agents.interaction.PersonBirthAgent;

import static grakn.simulation.db.common.agents.world.CityAgentRunner.SessionStrategy.COUNTRY;

public class Neo4jAgentPicker extends AgentPicker {
    @Override
    protected CityAgentRunner marriage() {
        return null;
    }

    @Override
    protected CityAgentRunner personBirth() {
        return new CityAgentRunner(PersonBirthAgent.class, COUNTRY);
    }

    @Override
    protected CityAgentRunner ageUpdate() {
        return null;
    }

    @Override
    protected CityAgentRunner parentship() {
        return null;
    }

    @Override
    protected CityAgentRunner relocation() {
        return null;
    }

    @Override
    protected CountryAgentRunner company() {
        return null;
    }

    @Override
    protected CityAgentRunner employment() {
        return null;
    }

    @Override
    protected ContinentAgentRunner product() {
        return null;
    }

    @Override
    protected CountryAgentRunner transaction() {
        return null;
    }

    @Override
    protected CityAgentRunner friendship() {
        return null;
    }
}
