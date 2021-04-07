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
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class PersonBirthAgent<TX extends Transaction> extends Agent<World.City, TX> {

    public PersonBirthAgent(Client<? extends Session<TX>, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<World.City> getRegions(World world) {
        return world.getCities().collect(toList());
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, World.City region, Random random) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        int numBirths = context.world().getScaleFactor();
        try (TX tx = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {
            for (int i = 0; i < numBirths; i++) {
                String gender;
                String forename;
                String surname = pickOne(context.world().getSurnames(), random);

                boolean genderBool = random.nextBoolean();
                if (genderBool) {
                    gender = "male";
                    forename = pickOne(context.world().getMaleForenames(), random);
                } else {
                    gender = "female";
                    forename = pickOne(context.world().getFemaleForenames(), random);
                }
                String email = "email/" + uniqueId(context, region.tracker(), i);
                runAction(actionFactory().insertPersonAction(tx, region, context.today(), email, gender, forename, surname), reports);
            }
            tx.commit();
        }

        return reports;
    }
}
