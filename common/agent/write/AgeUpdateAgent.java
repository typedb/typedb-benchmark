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
import grakn.simulation.common.action.write.UpdateAgesOfPeopleInCityAction;
import grakn.simulation.common.agent.region.CityAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;

import java.util.Random;

public class AgeUpdateAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public AgeUpdateAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory) {
        super(dbDriver, actionFactory);
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
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city, SimulationContext simulationContext) {
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), trace())) {
                UpdateAgesOfPeopleInCityAction<DB_OPERATION> updateAgesOfAllPeopleInCityAction = actionFactory().updateAgesOfPeopleInCityAction(dbOperation, simulationContext.today(), city);
                runAction(updateAgesOfAllPeopleInCityAction);
                dbOperation.save();
            }
        }
    }
}
