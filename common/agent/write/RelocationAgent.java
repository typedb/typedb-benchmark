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
import grakn.benchmark.common.action.read.CitiesInContinentAction;
import grakn.benchmark.common.action.read.ResidentsInCityAction;
import grakn.benchmark.common.agent.base.Allocation;
import grakn.benchmark.common.agent.region.CityAgent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;
import grakn.benchmark.common.driver.DbOperationFactory;
import grakn.benchmark.common.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RelocationAgent<DB_OPERATION extends DbOperation> extends CityAgent<DB_OPERATION> {

    public RelocationAgent(DbDriver<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.common.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
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
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

            LocalDateTime earliestDateOfResidencyToRelocate;
            earliestDateOfResidencyToRelocate = benchmarkContext.today().minusYears(2);

            List<String> residentEmails;
            List<String> relocationCityNames;

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                ResidentsInCityAction<?> residentsInCityAction = actionFactory().residentsInCityAction(dbOperation, city, benchmarkContext.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
                residentEmails = runAction(residentsInCityAction);
            }
            shuffle(residentEmails);

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                CitiesInContinentAction<?> citiesInContinentAction = actionFactory().citiesInContinentAction(dbOperation, city);
                relocationCityNames = runAction(citiesInContinentAction);
            }

            try (DB_OPERATION dbOperation = dbOperationFactory.newDbOperation(tracker(), iteration(), trace())) {
                Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                    runAction(actionFactory().insertRelocationAction(dbOperation, city, benchmarkContext.today(), residentEmail, relocationCityName));
                });
                dbOperation.save();
            }
        }
    }
}