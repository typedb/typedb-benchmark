package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.action.ResidentsInCityAction;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.agents.region.CityAgent;
import grakn.simulation.db.common.context.DbDriver;
import grakn.simulation.db.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class FriendshipAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public FriendshipAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalFriendshipAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalFriendshipAgent(simulationStep, tracker, random, test);
    }

    public class RegionalFriendshipAgent extends RegionalAgent {
        public RegionalFriendshipAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
            List<String> residentEmails;
            ResidentsInCityAction<?> residentEmailsAction = dbOperationController.actionFactory().residentsInCityAction(city, simulationContext.world().getScaleFactor(), simulationContext.today());
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(residentEmailsAction, tracker())) {
                residentEmails = runAction(residentEmailsAction);
            } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertFriendshipAction", tracker())) {
                if (residentEmails.size() > 0) {
                    shuffle(residentEmails);
                    int numFriendships = simulationContext.world().getScaleFactor();
                    for (int i = 0; i < numFriendships; i++) {
                        InsertFriendshipAction insertFriendshipAction = dbOperationController.actionFactory().insertFriendshipAction(simulationContext.today(), pickOne(residentEmails), pickOne(residentEmails));
                        runAction(insertFriendshipAction);
                    }
                    dbOperation.save();
                }
            }
        }
    }

    enum FriendshipField implements DbOperationController.ComparableField {
        FRIEND1_EMAIL, FRIEND2_EMAIL, START_DATE
    }
}
