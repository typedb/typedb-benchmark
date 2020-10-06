package grakn.simulation.db.common.agent.interaction;

import grakn.simulation.db.common.action.read.BirthsInCityAction;
import grakn.simulation.db.common.action.read.MarriedCoupleAction;
import grakn.simulation.db.common.operation.DbOperationController;
import grakn.simulation.db.common.SimulationContext;
import grakn.simulation.db.common.agent.region.CityAgent;
import grakn.simulation.db.common.agent.utils.Allocation;
import grakn.simulation.db.common.driver.DbDriver;
import grakn.simulation.db.common.world.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParentshipAgent<DB_DRIVER extends DbDriver> extends CityAgent<DB_DRIVER> {

    public ParentshipAgent(DB_DRIVER dbDriver) {
        super(dbDriver);
    }

    @Override
    protected RegionalParentshipAgent getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new RegionalParentshipAgent(simulationStep, tracker, random, test);
    }

    public class RegionalParentshipAgent extends RegionalAgent {
        public RegionalParentshipAgent(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationController dbOperationController, World.City city, SimulationContext simulationContext) {
            // Query for married couples in the city who are not already in a parentship relation together
            List<String> childrenEmails;

            BirthsInCityAction<?> birthsInCityAction = dbOperationController.actionFactory().birthsInCityAction(city, simulationContext.today());
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(birthsInCityAction, tracker())) {
                childrenEmails = runAction(birthsInCityAction);
            }

            List<HashMap<SpouseType, String>> marriedCouple;

            MarriedCoupleAction<?> marriedCoupleAction = dbOperationController.actionFactory().marriedCoupleAction(city, simulationContext.today());
            try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation(marriedCoupleAction, tracker())) {
                marriedCouple = runAction(marriedCoupleAction);
            }

            if (marriedCouple.size() > 0 && childrenEmails.size() > 0) {
                try (DbOperationController.DbOperation dbOperation = dbOperationController.newDbOperation("InsertParentship", tracker())) {
                    LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriedCouple.size());
                    for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                        Integer marriageIndex = childrenForMarriage.getKey();
                        List<Integer> children = childrenForMarriage.getValue();
                        HashMap<SpouseType, String> marriage = marriedCouple.get(marriageIndex);

                        for (Integer childIndex : children) {
                            String childEmail = childrenEmails.get(childIndex);
                            runAction(dbOperationController.actionFactory().insertParentship(marriage, childEmail));
                        }
                    }
                    dbOperation.save();
                }
            }
        }
    }

    public enum SpouseType {
        WIFE, HUSBAND
    }
}
