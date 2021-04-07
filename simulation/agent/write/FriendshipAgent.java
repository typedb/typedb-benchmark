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
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class FriendshipAgent<TX extends Transaction> extends CityAgent<TX> {

    public FriendshipAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(dbDriver, actionFactory, context);
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World.City region, Random random) {
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        List<String> residentEmails;
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            ResidentsInCityAction<?> residentEmailsAction = actionFactory().residentsInCityAction(dbOperation, region, context.world().getScaleFactor(), context.today());
            residentEmails = runAction(residentEmailsAction, reports);
        } // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585

        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            if (residentEmails.size() > 0) {
                shuffle(residentEmails, random);
                int numFriendships = context.world().getScaleFactor();
                for (int i = 0; i < numFriendships; i++) {
                    runAction(actionFactory().insertFriendshipAction(dbOperation, context.today(), pickOne(residentEmails, random), pickOne(residentEmails, random)), reports);
                }
                dbOperation.commit();
            }
        }

        return reports;
    }
}
