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
import grakn.benchmark.simulation.action.read.CitiesInContinentAction;
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
import grakn.benchmark.simulation.agent.base.Allocation;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgentManager;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class RelocationAgent<TX extends Transaction> extends CityAgentManager<TX> {

    public RelocationAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(int iteration, String tracker, Random random, boolean test) {
        return new City(iteration, tracker, random, test);
    }

    public class City extends CityAgent {
        public City(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<TX> session, World.City city) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

            LocalDateTime earliestDateOfResidencyToRelocate;
            earliestDateOfResidencyToRelocate = context.today().minusYears(2);

            List<String> residentEmails;
            List<String> relocationCityNames;

            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                ResidentsInCityAction<?> residentsInCityAction = actionFactory().residentsInCityAction(dbOperation, city, context.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
                residentEmails = runAction(residentsInCityAction, isTest(), actionReports());
            }
            shuffle(residentEmails, random());

            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                CitiesInContinentAction<?> citiesInContinentAction = actionFactory().citiesInContinentAction(dbOperation, city);
                relocationCityNames = runAction(citiesInContinentAction, isTest(), actionReports());
            }

            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                    runAction((Action<?, ?>) actionFactory().insertRelocationAction(dbOperation, city, context.today(), residentEmail, relocationCityName), isTest(), actionReports());
                });
                dbOperation.commit();
            }
        }
    }
}