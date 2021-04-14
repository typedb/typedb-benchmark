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
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class CompanyAgent<TX extends Transaction> extends Agent<GeoData.Country, TX> {

    public CompanyAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.Country> regions() {
        return context.geoData().countries();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.Country region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        int numCompanies = context.scaleFactor();
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            for (int i = 0; i < numCompanies; i++) {
                String adjective = pickOne(context.wordData().getAdjectives(), random);
                String noun = pickOne(context.wordData().getNouns(), random);
                int companyNumber = uniqueId(context, region.tracker(), i).hashCode();
                String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber; // TODO: ???
                insertCompany(tx, region, context.today(), companyNumber, companyName);
            }
            tx.commit();
        }
        return reports;
    }

    protected abstract void insertCompany(TX tx, GeoData.Country region, LocalDateTime today, int companyNumber, String companyName);
}