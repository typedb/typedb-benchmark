package grakn.simulation.db.grakn.initialise;

import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.initialise.AgentPicker;
import grakn.simulation.db.grakn.context.GraknContext;
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

public class GraknAgentPicker extends AgentPicker<GraknContext> {

    private final GraknContext backendContext;

    public GraknAgentPicker(GraknContext backendContext) {
        this.backendContext = backendContext;
    }

    @Override
    protected CityAgentRunner<GraknContext> marriage() {
        return new CityAgentRunner<>(MarriageAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected CityAgentRunner<GraknContext> personBirth() {
        return new CityAgentRunner<>(PersonBirthAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected CityAgentRunner<GraknContext> ageUpdate() {
        return new CityAgentRunner<>(AgeUpdateAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected CityAgentRunner<GraknContext> parentship() {
        return new CityAgentRunner<>(ParentshipAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected CityAgentRunner<GraknContext> relocation() {
        return new CityAgentRunner<>(RelocationAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected CountryAgentRunner<GraknContext> company() {
        return new CountryAgentRunner<>(CompanyAgent.class, backendContext, COUNTRY);
    }

    @Override
    protected CityAgentRunner<GraknContext> employment() {
        return new CityAgentRunner<>(EmploymentAgent.class, backendContext, CONTINENT);
    }

    @Override
    protected ContinentAgentRunner<GraknContext> product() {
        return new ContinentAgentRunner<>(ProductAgent.class, backendContext);
    }

    @Override
    protected ContinentAgentRunner<GraknContext> transaction() {
        return new ContinentAgentRunner<>(TransactionAgent.class, backendContext);
    }

    @Override
    protected CityAgentRunner<GraknContext> friendship() {
        return new CityAgentRunner<>(FriendshipAgent.class, backendContext, CONTINENT);
    }

}
