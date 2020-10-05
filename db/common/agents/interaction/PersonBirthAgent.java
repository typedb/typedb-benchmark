package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class PersonBirthAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public PersonBirthAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
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
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            int numBirths = simulationContext.world().getScaleFactor();
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("insertPerson", tracker())) {
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
                    runAction(dbOperationController.actionFactory().insertPerson(city, simulationContext.today(), email, gender, forename, surname));
                }
                dbOperation.save();
            }
        }
    }

    enum PersonBirthAgentField implements DbOperationController.ComparableField {
        EMAIL, GENDER, FORENAME, SURNAME, DATE_OF_BIRTH
    }
}
