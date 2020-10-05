package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DbDriver;

public class AgentFactory<DB_DRIVER extends DbDriver> {

    private final DB_DRIVER dbDriver;

    public AgentFactory(DB_DRIVER dbDriver) {
        this.dbDriver = dbDriver;
    }

    //    protected abstract MarriageAgent marriage();
//    protected abstract CityAgent<DB_DRIVER> personBirth();
//    protected abstract CityAgent<DB_DRIVER> ageUpdate();
//    protected abstract CityAgent<DB_DRIVER> parentship();
//    protected abstract CityAgent<DB_DRIVER> relocation();
//    protected abstract CountryAgent<DB_DRIVER> company();
    public EmploymentAgent<DB_DRIVER> employment() {
        return new EmploymentAgent<>(dbDriver, CityAgent.SessionStrategy.CITY);
    }
//    protected abstract ContinentAgent<DB_DRIVER> product();
//    protected abstract ContinentAgent<DB_DRIVER> transaction();
//    protected abstract CityAgent<DB_DRIVER> friendship();

    public Agent<?, DB_DRIVER> get(String agentName) {
        switch (agentName) {
//            case "marriage":
//                return marriage();
//            case "personBirth":
//                return personBirth();
//            case "ageUpdate":
//                return ageUpdate();
//            case "parentship":
//                return parentship();
//            case "relocation":
//                return relocation();
//            case "company":
//                return company();
            case "employment":
                return employment();
//            case "product":
//                return product();
//            case "transaction":
//                return transaction();
//            case "friendship":
//                return friendship();
            default:
                throw new IllegalArgumentException("Unrecognised agent name: " + agentName);
        }
    }
}
