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

package grakn.benchmark.common.agent.write;

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.action.read.ResidentsInCityAction;
import grakn.benchmark.common.agent.region.CityAgent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.world.World;

import java.util.List;
import java.util.Random;

public class FriendshipAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public FriendshipAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.common.agent.base.BenchmarkContext benchmarkContext) {
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
            List<String> residentEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
                ResidentsInCityAction<?> residentEmailsAction = actionFactory().residentsInCityAction(dbOperation, city, benchmarkContext.world().getScaleFactor(), benchmarkContext.today());
                residentEmails = runAction(residentEmailsAction);
            } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), isTracing())) {
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
