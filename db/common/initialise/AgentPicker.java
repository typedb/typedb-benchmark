package grakn.simulation.db.common.initialise;

import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;
import grakn.simulation.db.common.context.DatabaseContext;

public abstract class AgentPicker<CONTEXT extends DatabaseContext> {

    protected abstract CityAgentRunner<CONTEXT> marriage();
    protected abstract CityAgentRunner<CONTEXT> personBirth();
    protected abstract CityAgentRunner<CONTEXT> ageUpdate();
    protected abstract CityAgentRunner<CONTEXT> parentship();
    protected abstract CityAgentRunner<CONTEXT> relocation();
    protected abstract CountryAgentRunner<CONTEXT> company();
    protected abstract CityAgentRunner<CONTEXT> employment();
    protected abstract ContinentAgentRunner<CONTEXT> product();
    protected abstract ContinentAgentRunner<CONTEXT> transaction();
    protected abstract CityAgentRunner<CONTEXT> friendship();

    public AgentRunner<?, CONTEXT> get(String agentName) {
        switch (agentName) {
            case "marriage":
                return marriage();
            case "personBirth":
                return personBirth();
            case "ageUpdate":
                return ageUpdate();
            case "parentship":
                return parentship();
            case "relocation":
                return relocation();
            case "company":
                return company();
            case "employment":
                return employment();
            case "product":
                return product();
            case "transaction":
                return transaction();
            case "friendship":
                return friendship();
            default:
                throw new IllegalArgumentException("Unrecognised agent name: " + agentName);
        }
    }

}
