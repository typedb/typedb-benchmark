package grakn.simulation.db.neo4j.initialise;

import grakn.simulation.db.common.agents.region.CityAgentRunner;
import grakn.simulation.db.common.agents.region.ContinentAgentRunner;
import grakn.simulation.db.common.agents.region.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.grakn.context.GraknContext;
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
import grakn.simulation.db.neo4j.common.Neo4jContext;

import static grakn.simulation.db.common.agents.region.CityAgentRunner.SessionStrategy.CITY;

public class Neo4jAgentPicker extends AgentPicker<Neo4jContext> {

    private final Neo4jContext backendContext;

    public Neo4jAgentPicker(Neo4jContext backendContext) {
        this.backendContext = backendContext;
    }

    @Override
    protected CityAgentRunner<Neo4jContext> marriage() {
        return new CityAgentRunner<>(MarriageAgent.class, backendContext, CITY);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> personBirth() {
        return new CityAgentRunner<>(PersonBirthAgent.class, backendContext, CITY);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> ageUpdate() {
        return new CityAgentRunner<>(AgeUpdateAgent.class, backendContext, CITY);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> parentship() {
        return new CityAgentRunner<>(ParentshipAgent.class, backendContext, CITY);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> relocation() {
        return new CityAgentRunner<>(RelocationAgent.class, backendContext, CITY);
    }

    @Override
    protected CountryAgentRunner<Neo4jContext> company() {
        return new CountryAgentRunner<>(CompanyAgent.class, backendContext, CountryAgentRunner.SessionStrategy.COUNTRY);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> employment() {
        return new CityAgentRunner<>(EmploymentAgent.class, backendContext, CITY);
    }

    @Override
    protected ContinentAgentRunner<Neo4jContext> product() {
        return new ContinentAgentRunner<>(ProductAgent.class, backendContext);
    }

    @Override
    protected ContinentAgentRunner<Neo4jContext> transaction() {
        return new ContinentAgentRunner<>(TransactionAgent.class, backendContext);
    }

    @Override
    protected CityAgentRunner<Neo4jContext> friendship() {
        return new CityAgentRunner<>(FriendshipAgent.class, backendContext, CITY);
    }
}
