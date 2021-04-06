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
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.util.List;
import java.util.Random;

public class FriendshipAgent<TX extends Transaction> extends CityAgent<TX> {

    public FriendshipAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
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
        protected void run(Session<TX> dbOperationFactory, World.City city) {
            List<String> residentEmails;
            try (TX dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                ResidentsInCityAction<?> residentEmailsAction = actionFactory().residentsInCityAction(dbOperation, city, benchmarkContext.world().getScaleFactor(), benchmarkContext.today());
                residentEmails = runAction(residentEmailsAction);
            } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

            try (TX dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                if (residentEmails.size() > 0) {
                    shuffle(residentEmails);
                    int numFriendships = benchmarkContext.world().getScaleFactor();
                    for (int i = 0; i < numFriendships; i++) {
                        runAction(actionFactory().insertFriendshipAction(dbOperation, benchmarkContext.today(), pickOne(residentEmails), pickOne(residentEmails)));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
