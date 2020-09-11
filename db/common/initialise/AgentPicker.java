package grakn.simulation.db.common.initialise;

import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;

public abstract class AgentPicker<C> {

    protected abstract CityAgentRunner<C> marriage();
    protected abstract CityAgentRunner<C> personBirth();
    protected abstract CityAgentRunner<C> ageUpdate();
    protected abstract CityAgentRunner<C> parentship();
    protected abstract CityAgentRunner<C> relocation();
    protected abstract CountryAgentRunner<C> company();
    protected abstract CityAgentRunner<C> employment();
    protected abstract ContinentAgentRunner<C> product();
    protected abstract ContinentAgentRunner<C> transaction();
    protected abstract CityAgentRunner<C> friendship();

    public AgentRunner<?, C> get(String agentName) {
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
