package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.SimulationContext;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class MarriageAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public MarriageAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalMarriageAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalMarriageAgent(simulationStep, tracker, random, test);
    }

    public class RegionalMarriageAgent extends RegionalAgent {
        public RegionalMarriageAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {

            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            LocalDateTime dobOfAdults = simulationContext.today().minusYears(simulationContext.world().AGE_OF_ADULTHOOD);
            List<String> womenEmails;
            UnmarriedPeopleInCityAction<?> unmarriedWomenInCityAction = dbOperationController.actionFactory().unmarriedPeopleInCityAction(city, "female", dobOfAdults);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(unmarriedWomenInCityAction, tracker())) {
                womenEmails = runAction(unmarriedWomenInCityAction);
            }
            shuffle(womenEmails);

            List<String> menEmails;
            UnmarriedPeopleInCityAction<?> unmarriedMenInCityAction = dbOperationController.actionFactory().unmarriedPeopleInCityAction(city, "male", dobOfAdults);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(unmarriedMenInCityAction, tracker())) {
                menEmails = runAction(unmarriedMenInCityAction);
            }
            shuffle(menEmails);

            int numMarriagesPossible = Math.min(simulationContext.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertMarriage", tracker())) {
                if (numMarriagesPossible > 0) {
                    for (int i = 0; i < numMarriagesPossible; i++) {
                        String wifeEmail = womenEmails.get(i);
                        String husbandEmail = menEmails.get(i);
                        int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();
                        runAction(dbOperationController.actionFactory().insertMarriageAction(city, marriageIdentifier, wifeEmail, husbandEmail));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
