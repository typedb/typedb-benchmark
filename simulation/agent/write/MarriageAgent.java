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
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class MarriageAgent<TX extends Transaction> extends CityAgent<TX> {

    public MarriageAgent(Client<TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World.City region, Random random) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        LocalDateTime dobOfAdults = context.today().minusYears(context.world().AGE_OF_ADULTHOOD);
        List<String> womenEmails;
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            womenEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, region, "female", dobOfAdults), reports);
            shuffle(womenEmails, random);
        }

        List<String> menEmails;
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            menEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, region, "male", dobOfAdults), reports);
            shuffle(menEmails, random);
        }

        int numMarriagesPossible = Math.min(context.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
        if (context.iteration() >= 5) {
            System.out.println("asdf");
            assert true;
        }
        try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            if (numMarriagesPossible > 0) {
                for (int i = 0; i < numMarriagesPossible; i++) {
                    String wifeEmail = womenEmails.get(i);
                    String husbandEmail = menEmails.get(i);
                    int marriageIdentifier = uniqueId(context, region.tracker(), i).hashCode();
                    runAction(actionFactory().insertMarriageAction(dbOperation, region, marriageIdentifier, wifeEmail, husbandEmail), reports);
                }
                dbOperation.commit();
            }
        }

        return reports;
    }
}
