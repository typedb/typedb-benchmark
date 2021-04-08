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
import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.common.World;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class CompanyAgent<TX extends Transaction> extends Agent<World.Country, TX> {

    public CompanyAgent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<World.Country> getRegions(World world) {
        return world.getCountries().collect(toList());
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World.Country region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        int numCompanies = context.world().getScaleFactor();
        try (TX tx = session.transaction(region.tracker(), context.iteration(), isTracing())) {
            for (int i = 0; i < numCompanies; i++) {
                String adjective = pickOne(context.world().getAdjectives(), random);
                String noun = pickOne(context.world().getNouns(), random);
                int companyNumber = uniqueId(context, region.tracker(), i).hashCode();
                String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                runAction(actionFactory().insertCompanyAction(tx, region, context.today(), companyNumber, companyName), reports);
            }
            tx.commit();
        }

        return reports;
    }
}