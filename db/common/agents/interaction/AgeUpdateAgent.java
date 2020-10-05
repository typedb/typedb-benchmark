package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.action.Action;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DbDriver;
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
            Action<?, ?> updateAgesOfAllPeopleInCityAction = dbOperationController.actionFactory().updateAgesOfPeopleInCityAction(simulationContext.today(), city);
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(updateAgesOfAllPeopleInCityAction, tracker())) {
                runAction(updateAgesOfAllPeopleInCityAction);
                dbOperation.save();
            }
        }
    }
}
