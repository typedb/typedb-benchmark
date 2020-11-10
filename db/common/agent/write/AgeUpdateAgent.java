package grakn.simulation.db.common.agent.write;

import grakn.simulation.db.common.agent.base.SimulationContext;
import grakn.simulation.db.common.action.ActionFactory;
import grakn.simulation.db.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.driver.DbOperation;
import grakn.simulation.db.common.driver.DbOperationFactory;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class AgeUpdateAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public AgeUpdateAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
    }

    @Override
    protected RegionalAgeUpdateAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalAgeUpdateAgent(simulationStep, tracker, random, test);
    }

    public class RegionalAgeUpdateAgent extends RegionalAgent {
        public RegionalAgeUpdateAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                UpdateAgesOfPeopleInCityAction<DB_OPERATION> updateAgesOfAllPeopleInCityAction = actionFactory().updateAgesOfPeopleInCityAction(dbOperation, simulationContext.today(), city);
                runAction(updateAgesOfAllPeopleInCityAction);
                dbOperation.save();
            }
        }
    }
}
