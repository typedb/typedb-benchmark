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

public abstract class RelocationAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public RelocationAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.City> regions() {
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
            residentEmails = matchResidentsInCity(tx, region, context.scaleFactor(), earliestDateOfResidencyToRelocate);
        }
        shuffle(residentEmails, random);

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            relocationCityNames = matchCitiesInContinent(tx, region);
        }

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            Allocation.allocate(residentEmails, relocationCityNames, (residentEmail, relocationCityName) -> {
                insertRelocation(tx, region, context.today(), residentEmail, relocationCityName);
            });
            tx.commit();
        }

        return reports;
    }

    protected abstract List<String> matchResidentsInCity(TX tx, GeoData.City region, int scaleFactor, LocalDateTime earliestDateOfResidencyToRelocate);

    protected abstract List<String> matchCitiesInContinent(TX tx, GeoData.City region);

    protected abstract void insertRelocation(TX tx, GeoData.City region, LocalDateTime today, String residentEmail, String relocationCityName);
}