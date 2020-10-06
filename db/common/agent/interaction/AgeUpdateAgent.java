package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.action.read.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.SimulationContext;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.Random;

public class AgeUpdateAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public AgeUpdateAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
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
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
            UpdateAgesOfPeopleInCityAction<?> updateAgesOfAllPeopleInCityAction = dbOperationController.actionFactory().updateAgesOfPeopleInCityAction(simulationContext.today(), city);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(updateAgesOfAllPeopleInCityAction, tracker())) {
                runAction(updateAgesOfAllPeopleInCityAction);
                dbOperation.save();
            }
        }
    }
}
