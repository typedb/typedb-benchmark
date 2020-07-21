package grakn.simulation.db.common.initialise;

import grakn.simulation.db.common.agents.base.AgentRunner;
import grakn.simulation.db.common.agents.world.CityAgentRunner;
import grakn.simulation.db.common.agents.world.ContinentAgentRunner;
import grakn.simulation.db.common.agents.world.CountryAgentRunner;

public abstract class AgentPicker {

    protected abstract CityAgentRunner marriage();
    protected abstract CityAgentRunner personBirth();
    protected abstract CityAgentRunner ageUpdate();
    protected abstract CityAgentRunner parentship();
    protected abstract CityAgentRunner relocation();
    protected abstract CountryAgentRunner company();
    protected abstract CityAgentRunner employment();
    protected abstract ContinentAgentRunner product();
    protected abstract ContinentAgentRunner transaction();
    protected abstract CityAgentRunner friendship();

    public AgentRunner<?> get(String agentName) {
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
