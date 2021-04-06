/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.simulation.agent.write;

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.action.read.BirthsInCityAction;
import grakn.benchmark.simulation.action.read.MarriedCoupleAction;
import grakn.benchmark.simulation.agent.base.Allocation;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.DbDriver;
import grakn.benchmark.simulation.driver.DbOperation;
import grakn.benchmark.simulation.driver.DbOperationFactory;
import grakn.benchmark.simulation.world.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParentshipAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public ParentshipAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Regional getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new City(iteration, tracker, random, test);
    }

    public class City extends CityRegion {
        public City(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city) {
            // Query for married couples in the city who are not already in a parentship relation together
            List<String> childrenEmails;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
                BirthsInCityAction<?> birthsInCityAction = actionFactory().birthsInCityAction(dbOperation, city, benchmarkContext.today());
                childrenEmails = runAction(birthsInCityAction);
            }

            List<HashMap<SpouseType, String>> marriedCouple;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
                MarriedCoupleAction<?> marriedCoupleAction = actionFactory().marriedCoupleAction(dbOperation, city, benchmarkContext.today());
                marriedCouple = runAction(marriedCoupleAction);
            }

            if (marriedCouple.size() > 0 && childrenEmails.size() > 0) {
                try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
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
