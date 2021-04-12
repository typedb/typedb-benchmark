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
import grakn.benchmark.simulation.common.SimulationContext;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.common.GeoData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PersonBirthAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public PersonBirthAgent(Client<?, TX> client, ActionFactory<TX, ?> actionFactory, SimulationContext context) {
        super(client, actionFactory, context);
    }

    @Override
    protected List<GeoData.City> getRegions() {
        return context.geoData().cities();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.City region, Random random) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        int numBirths = context.scaleFactor();
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            for (int i = 0; i < numBirths; i++) {
                String gender;
                String forename;
                String surname = pickOne(context.wordData().getSurnames(), random);

                boolean genderBool = random.nextBoolean();
                if (genderBool) {
                    gender = "male";
                    forename = pickOne(context.wordData().getMaleForenames(), random);
                } else {
                    gender = "female";
                    forename = pickOne(context.wordData().getFemaleForenames(), random);
                }
                String email = "email/" + uniqueId(context, region.tracker(), i);
                runAction(actionFactory().insertPersonAction(tx, region, context.today(), email, gender, forename, surname), reports);
            }
            tx.commit();
        }

        return reports;
    }
}
