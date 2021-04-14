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

public abstract class MarriageAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public enum SpouseType {
        WIFE, HUSBAND
    }

    public MarriageAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.City> regions() {
        return context.geoData().cities();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.City region, Random random) {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        LocalDateTime dobOfAdults = context.today().minusYears(SimulationContext.AGE_OF_ADULTHOOD);
        List<String> womenEmails;
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            womenEmails = matchUnmarriedPeopleInCity(tx, region, "female", dobOfAdults);
            shuffle(womenEmails, random);
        }


        List<String> menEmails;
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            menEmails = matchUnmarriedPeopleInCity(tx, region, "male", dobOfAdults);
            shuffle(menEmails, random);
        }

        int numMarriagesPossible = Math.min(context.scaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            if (numMarriagesPossible > 0) {
                for (int i = 0; i < numMarriagesPossible; i++) {
                    String wifeEmail = womenEmails.get(i);
                    String husbandEmail = menEmails.get(i);
                    int marriageIdentifier = uniqueId(context, region.tracker(), i).hashCode();
                    insertMarriage(tx, region, marriageIdentifier, wifeEmail, husbandEmail); // TODO: we should pass current date
                }
                tx.commit();
            }
        }

        return reports;
    }

    protected abstract List<String> matchUnmarriedPeopleInCity(TX tx, GeoData.City region, String female, LocalDateTime dobOfAdults);

    protected abstract void insertMarriage(TX tx, GeoData.City region, int marriageIdentifier, String wifeEmail, String husbandEmail);
}
