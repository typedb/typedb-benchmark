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

package grakn.simulation.common.agent.write;

import grakn.simulation.common.agent.base.SimulationContext;
import grakn.simulation.common.action.ActionFactory;
import grakn.simulation.common.action.read.ResidentsInCityAction;
import grakn.simulation.common.agent.region.CityAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;

import java.util.List;
import java.util.Random;

public class FriendshipAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public FriendshipAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, SimulationContext simulationContext) {
        super(dbDriver, actionFactory, simulationContext);
    }

    @Override
    protected City getRegionalAgent(int simulationStep, String tracker, Random random, boolean test) {
        return new City(simulationStep, tracker, random, test);
    }

    public class City extends CityRegion {
        public City(int simulationStep, String tracker, Random random, boolean test) {
            super(simulationStep, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city) {
            List<String> residentEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                ResidentsInCityAction<?> residentEmailsAction = actionFactory().residentsInCityAction(dbOperation, city, simulationContext.world().getScaleFactor(), simulationContext.today());
                residentEmails = runAction(residentEmailsAction);
            } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                if (residentEmails.size() > 0) {
                    shuffle(residentEmails);
                    int numFriendships = simulationContext.world().getScaleFactor();
                    for (int i = 0; i < numFriendships; i++) {
                        runAction(actionFactory().insertFriendshipAction(dbOperation, simulationContext.today(), pickOne(residentEmails), pickOne(residentEmails)));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
