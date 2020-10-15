package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class PersonBirthAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends CityAgent<DB_DRIVER, DB_OPERATION> {

    public PersonBirthAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalPersonBirthAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalPersonBirthAgent(simulationStep, tracker, random, test);
    }

    public class RegionalPersonBirthAgent extends RegionalAgent {
        public RegionalPersonBirthAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            int numBirths = simulationContext.world().getScaleFactor();
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                for (int i = 0; i < numBirths; i++) {
                    String gender;
                    String forename;
                    String surname = pickOne(simulationContext.world().getSurnames());

                    boolean genderBool = random().nextBoolean();
                    if (genderBool) {
                        gender = "male";
                        forename = pickOne(simulationContext.world().getMaleForenames());
                    } else {
                        gender = "female";
                        forename = pickOne(simulationContext.world().getFemaleForenames());
                    }
                    // Email is used as a key and needs to be unique, which requires a lot of information
                    String email = forename + "."
                            + surname + "_"
                            + simulationContext.today().toString() + "_"
                            + i + "_"
                            + simulationContext.simulationStep() + "_"
                            + city + "_"
                            + city.country() + "_"
                            + city.country().continent()
                            + "@gmail.com";
                    runAction(actionFactory().insertPersonAction(dbOperation, city, simulationContext.today(), email, gender, forename, surname));
                }
                dbOperation.save();
            }
        }
    }
}
