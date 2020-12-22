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
import grakn.simulation.common.agent.region.CityAgent;
import grakn.simulation.common.driver.DbDriver;
import grakn.simulation.common.driver.DbOperation;
import grakn.simulation.common.driver.DbOperationFactory;
import grakn.simulation.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class MarriageAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public MarriageAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, SimulationContext simulationContext) {
        super(dbDriver, actionFactory, simulationContext);
    }

    @Override
    protected Region getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new City(iteration, tracker, random, test);
    }

    public class City extends CityRegion {
        public City(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(DbOperationFactory<DB_OPERATION> dbOperationFactory, World.City city) {

            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            LocalDateTime dobOfAdults = simulationContext.today().minusYears(simulationContext.world().AGE_OF_ADULTHOOD);
            List<String> womenEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                womenEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "female", dobOfAdults));
                shuffle(womenEmails);
            }

            List<String> menEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                menEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "male", dobOfAdults));
                shuffle(menEmails);
            }

            int numMarriagesPossible = Math.min(simulationContext.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                if (numMarriagesPossible > 0) {
                    for (int i = 0; i < numMarriagesPossible; i++) {
                        String wifeEmail = womenEmails.get(i);
                        String husbandEmail = menEmails.get(i);
                        int marriageIdentifier = uniqueId(simulationContext, i).hashCode();
                        runAction(actionFactory().insertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
