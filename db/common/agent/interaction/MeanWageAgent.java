package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.read.MeanWageOfPeopleInWorldAction;
import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.agent.region.WorldAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class MeanWageAgent<DB_DRIVER extends DbDriver<DB_OPERATION>, DB_OPERATION extends DbOperation> extends WorldAgent<DB_DRIVER, DB_OPERATION> {
    public MeanWageAgent(DB_DRIVER dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalMeanWageAgent(simulationStep, tracker, random, test);
    }

    public class RegionalMeanWageAgent extends RegionalAgent {

        public RegionalMeanWageAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World world, SimulationContext simulationContext) {
            for (int i = 0; i <= simulationContext.world().getScaleFactor(); i++) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker())) {
                    MeanWageOfPeopleInWorldAction<DB_OPERATION> meanWageOfPeopleInCityAction = actionFactory().meanWageOfPeopleInWorldAction(dbOperation);
                    runAction(meanWageOfPeopleInCityAction);
                }
            }
        }
    }
}
