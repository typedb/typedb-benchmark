package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.driver.DbDriver;

public class AgentFactory<DB_DRIVER extends DbDriver> {

    private final DB_DRIVER dbDriver;

    public AgentFactory(DB_DRIVER dbDriver) {
        this.dbDriver = dbDriver;
    }

    public MarriageAgent<DB_DRIVER> marriage() {
        return new MarriageAgent<>(dbDriver);
    }

    public PersonBirthAgent<DB_DRIVER> personBirth() {
        return new PersonBirthAgent<>(dbDriver);
    }

    public AgeUpdateAgent<DB_DRIVER> ageUpdate() {
        return new AgeUpdateAgent<>(dbDriver);
    }

    public ParentshipAgent<DB_DRIVER> parentship() {
        return new ParentshipAgent<>(dbDriver);
    }

    public RelocationAgent<DB_DRIVER> relocation() {
        return new RelocationAgent<>(dbDriver);
    }

    public CompanyAgent<DB_DRIVER> company() {
        return new CompanyAgent<>(dbDriver);
    }

    public EmploymentAgent<DB_DRIVER> employment() {
        return new EmploymentAgent<>(dbDriver);
    }

    public ProductAgent<DB_DRIVER> product() {
        return new ProductAgent<>(dbDriver);
    }

    public TransactionAgent<DB_DRIVER> transaction() {
        return new TransactionAgent<>(dbDriver);
    }

    public FriendshipAgent<DB_DRIVER> friendship() {
        return new FriendshipAgent<>(dbDriver);
    }

    public Agent<?, DB_DRIVER> get(String agentName) {
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
