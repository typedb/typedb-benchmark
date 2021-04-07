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
    protected Agent getAgent(World.City region, Random random, SimulationContext context) {
        return new City(region, random, context);
    }

    public class City extends CityAgent {

        public City(World.City region, Random random, SimulationContext context) {
            super(region, random, context);
        }

        @Override
        protected void run(Session<TX> session, World.City region, List<Action<?, ?>.Report> reports, Random random) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

            LocalDateTime earliestDateOfResidencyToRelocate;
            earliestDateOfResidencyToRelocate = context.today().minusYears(2);

            List<String> residentEmails;
            List<String> relocationCityNames;

            try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
                ResidentsInCityAction<?> residentsInCityAction = actionFactory().residentsInCityAction(dbOperation, region, context.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
                residentEmails = runAction(residentsInCityAction, context.isTest(), reports);
            }
            shuffle(residentEmails, random);

            try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
                CitiesInContinentAction<?> citiesInContinentAction = actionFactory().citiesInContinentAction(dbOperation, region);
                relocationCityNames = runAction(citiesInContinentAction, context.isTest(), reports);
            }

            try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
                Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                    runAction((Action<?, ?>) actionFactory().insertRelocationAction(dbOperation, region, context.today(), residentEmail, relocationCityName), context.isTest(), reports);
                });
                dbOperation.commit();
            }
        }
    }
}