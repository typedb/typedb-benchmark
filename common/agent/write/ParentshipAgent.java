package grakn.simulation.common.agent.write;

import grakn.simulation.common.action.SpouseType;
import grakn.simulation.common.agent.base.SimulationContext;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.read.BirthsInCityAction;
import grakn.simulation.common.action.read.MarriedCoupleAction;
import grakn.simulation.common.agent.region.CityAgent;
import grakn.simulation.common.agent.base.Allocation;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParentshipAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public ParentshipAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
            // Query for married couples in the city who are not already in a parentship relation together
            List<String> childrenEmails;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                BirthsInCityAction<?> birthsInCityAction = actionFactory().birthsInCityAction(dbOperation, city, simulationContext.today());
                childrenEmails = runAction(birthsInCityAction);
            }

            List<HashMap<SpouseType, String>> marriedCouple;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                MarriedCoupleAction<?> marriedCoupleAction = actionFactory().marriedCoupleAction(dbOperation, city, simulationContext.today());
                marriedCouple = runAction(marriedCoupleAction);
            }

            if (marriedCouple.size() > 0 && childrenEmails.size() > 0) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                    LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriedCouple.size());
                    for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                        Integer marriageIndex = childrenForMarriage.getKey();
                        List<Integer> children = childrenForMarriage.getValue();
                        HashMap<SpouseType, String> marriage = marriedCouple.get(marriageIndex);

                        for (Integer childIndex : children) {
                            String childEmail = childrenEmails.get(childIndex);
                            runAction(actionFactory().insertParentshipAction(dbOperation, marriage, childEmail));
                        }
                    }
                    dbOperation.save();
                }
            }
        }
    }

}
