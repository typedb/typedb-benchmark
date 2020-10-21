package grakn.simulation.db.common.agent.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.UnmarriedPeopleInCityAction;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class MarriageAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends CityAgent<DB_DRIVER, DB_OPERATION> {

    public MarriageAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {

            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            LocalDateTime dobOfAdults = simulationContext.today().minusYears(simulationContext.world().AGE_OF_ADULTHOOD);
            List<String> womenEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                UnmarriedPeopleInCityAction<?> unmarriedWomenInCityAction = actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "female", dobOfAdults);
                womenEmails = runAction(unmarriedWomenInCityAction);
            }
            shuffle(womenEmails);

            List<String> menEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                UnmarriedPeopleInCityAction<?> unmarriedMenInCityAction = actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "male", dobOfAdults);
                menEmails = runAction(unmarriedMenInCityAction);
            }
            shuffle(menEmails);

            int numMarriagesPossible = Math.min(simulationContext.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                if (numMarriagesPossible > 0) {
                    for (int i = 0; i < numMarriagesPossible; i++) {
                        String wifeEmail = womenEmails.get(i);
                        String husbandEmail = menEmails.get(i);
                        int marriageIdentifier = uniqueId(simulationContext, i).hashCode();
                        runAction(actionFactory().insertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
