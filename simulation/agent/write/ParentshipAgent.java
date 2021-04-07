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

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.action.SpouseType;
import grakn.benchmark.simulation.action.read.BirthsInCityAction;
import grakn.benchmark.simulation.action.read.MarriedCoupleAction;
import grakn.benchmark.simulation.agent.base.Allocation;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParentshipAgent<TX extends Transaction> extends CityAgent<TX> {

    public ParentshipAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(dbDriver, actionFactory, context);
    }

    @Override
    protected void run(Session<TX> session, World.City region, List<Action<?, ?>.Report> reports, Random random) {
        // Query for married couples in the city who are not already in a parentship relation together
        List<String> childrenEmails;

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            BirthsInCityAction<?> birthsInCityAction = actionFactory().birthsInCityAction(dbOperation, region, context.today());
            childrenEmails = runAction(birthsInCityAction, context.isTest(), reports);
        }

        List<HashMap<SpouseType, String>> marriedCouple;

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            MarriedCoupleAction<?> marriedCoupleAction = actionFactory().marriedCoupleAction(dbOperation, region, context.today());
            marriedCouple = runAction(marriedCoupleAction, context.isTest(), reports);
        }

        if (marriedCouple.size() > 0 && childrenEmails.size() > 0) {
            try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
                LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriedCouple.size());
                for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                    Integer marriageIndex = childrenForMarriage.getKey();
                    List<Integer> children = childrenForMarriage.getValue();
                    HashMap<SpouseType, String> marriage = marriedCouple.get(marriageIndex);

                    for (Integer childIndex : children) {
                        String childEmail = childrenEmails.get(childIndex);
                        runAction((Action<?, ?>) actionFactory().insertParentshipAction(dbOperation, marriage, childEmail), context.isTest(), reports);
                    }
                }
                dbOperation.commit();
            }
        }
    }
}
