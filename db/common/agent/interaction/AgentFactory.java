package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;

public class AgentFactory<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation, ACTION_FACTORY extends ActionFactory<DB_OPERATION, ?>> {

    private final DB_DRIVER dbDriver;
    private final ACTION_FACTORY actionFactory;

    public AgentFactory(DB_DRIVER dbDriver, ACTION_FACTORY actionFactory) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
    }

    public MarriageAgent<DB_DRIVER, DB_OPERATION> marriage() {
        return new MarriageAgent<>(dbDriver, actionFactory);
    }

    public PersonBirthAgent<DB_DRIVER, DB_OPERATION> personBirth() {
        return new PersonBirthAgent<>(dbDriver, actionFactory);
    }

    public AgeUpdateAgent<DB_DRIVER, DB_OPERATION> ageUpdate() {
        return new AgeUpdateAgent<>(dbDriver, actionFactory);
    }

    public ParentshipAgent<DB_DRIVER, DB_OPERATION> parentship() {
        return new ParentshipAgent<>(dbDriver, actionFactory);
    }

    public RelocationAgent<DB_DRIVER, DB_OPERATION> relocation() {
        return new RelocationAgent<>(dbDriver, actionFactory);
    }

    public CompanyAgent<DB_DRIVER, DB_OPERATION> company() {
        return new CompanyAgent<>(dbDriver, actionFactory);
    }

    public EmploymentAgent<DB_DRIVER, DB_OPERATION> employment() {
        return new EmploymentAgent<>(dbDriver, actionFactory);
    }

    public ProductAgent<DB_DRIVER, DB_OPERATION> product() {
        return new ProductAgent<>(dbDriver, actionFactory);
    }

    public TransactionAgent<DB_DRIVER, DB_OPERATION> transaction() {
        return new TransactionAgent<>(dbDriver, actionFactory);
    }

    public FriendshipAgent<DB_DRIVER, DB_OPERATION> friendship() {
        return new FriendshipAgent<>(dbDriver, actionFactory);
    }

    public MeanWageAgent<DB_DRIVER, DB_OPERATION> meanWage() {
        return new MeanWageAgent<>(dbDriver, actionFactory);
    }

    public Agent<?, DB_DRIVER, DB_OPERATION> get(String agentName) {
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
            case "meanWage":
                return meanWage();
            default:
                throw new IllegalArgumentException("Unrecognised agent name: " + agentName);
        }
    }
}
