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

import grakn.benchmark.common.params.Context;
import grakn.benchmark.common.seed.SeedData;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PersonAgent<TX extends Transaction> extends Agent<SeedData.Country, TX>{

    protected PersonAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected List<SeedData.Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, SeedData.Country country, Random random) {
        List<Report> reports = new ArrayList<>();
        try (TX tx = session.transaction(country.tracker(), context.iterationNumber())) {
            insertPerson(tx);
            tx.commit();
        }
        return reports;
    }

    protected abstract void insertPerson(TX tx);
}
