/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.simulation.agent;

import grakn.benchmark.simulation.action.Action;
import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.common.Allocation;
import grakn.benchmark.simulation.common.GeoData;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class RelocationAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public RelocationAgent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<GeoData.City> getRegions() {
        return context.geoData().cities();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.City region, Random random) {
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

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            residentEmails = runAction(actionFactory().residentsInCityAction(tx, region, context.scaleFactor(), earliestDateOfResidencyToRelocate), reports);
        }
        shuffle(residentEmails, random);

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            relocationCityNames = runAction(actionFactory().citiesInContinentAction(tx, region), reports);
        }

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                runAction(actionFactory().insertRelocationAction(tx, region, context.today(), residentEmail, relocationCityName), reports);
            });
            tx.commit();
        }

        return reports;
    }
}