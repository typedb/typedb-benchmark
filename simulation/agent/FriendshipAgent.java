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

public abstract class FriendshipAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public FriendshipAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.City> regions() {
        return context.geoData().cities();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.City region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        List<String> residentEmails;
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            residentEmails = matchResidentsInCity(tx, region, context.scaleFactor(), context.today());
        } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            if (residentEmails.size() > 0) {
                shuffle(residentEmails, random);
                int numFriendships = context.scaleFactor();
                for (int i = 0; i < numFriendships; i++) {
                    insertFriendship(tx, context.today(), pickOne(residentEmails, random), pickOne(residentEmails, random));
                }
                tx.commit();
            }
        }

        return reports;
    }

    protected abstract List<String> matchResidentsInCity(TX tx, GeoData.City region, int scaleFactor, LocalDateTime today);

    protected abstract void insertFriendship(TX tx, LocalDateTime today, String email1, String email2);
}
