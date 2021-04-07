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
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.agent.base.Allocation;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;

public class RelocationAgent<TX extends Transaction> extends Agent<World.City, TX> {

    public RelocationAgent(Client<TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<World.City> getRegions(World world) {
        return world.getCities().collect(toList());
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World.City region, Random random) {
        /*
        Find people currently resident the city
        Find other cities in the continent
        Distribute the people among those cities via a relocation
         */

        List<Action<?, ?>.Report> reports = new ArrayList<>();
        LocalDateTime earliestDateOfResidencyToRelocate;
        earliestDateOfResidencyToRelocate = context.today().minusYears(2);

        List<String> residentEmails;
        List<String> relocationCityNames;

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            ResidentsInCityAction<?> residentsInCityAction = actionFactory().residentsInCityAction(dbOperation, region, context.world().getScaleFactor(), earliestDateOfResidencyToRelocate);
            residentEmails = runAction(residentsInCityAction, reports);
        }
        shuffle(residentEmails, random);

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            CitiesInContinentAction<?> citiesInContinentAction = actionFactory().citiesInContinentAction(dbOperation, region);
            relocationCityNames = runAction(citiesInContinentAction, reports);
        }

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                runAction(actionFactory().insertRelocationAction(dbOperation, region, context.today(), residentEmail, relocationCityName), reports);
            });
            dbOperation.commit();
        }

        return reports;
    }
}