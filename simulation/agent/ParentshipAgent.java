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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class ParentshipAgent<TX extends Transaction> extends Agent<GeoData.City, TX> {

    public ParentshipAgent(Client<?, TX> client, SimulationContext context) {
        super(client, context);
    }

    @Override
    protected List<GeoData.City> regions() {
        return context.geoData().cities();
    }

    @Override
    protected List<Action<?, ?>.Report> run(Session<TX> session, GeoData.City region, Random random) {
        // Query for married couples in the city who are not already in a parentship relation together
        List<Action<?, ?>.Report> reports = new ArrayList<>();
        List<String> childrenEmails;

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            childrenEmails = matchBirthsInCity(tx, region, context.today());
        }

        List<HashMap<MarriageAgent.SpouseType, String>> marriedCouple;

        try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
            marriedCouple = matchMarriedCouple(tx, region);
        }

        if (marriedCouple.size() > 0 && childrenEmails.size() > 0) {
            try (TX tx = session.transaction(region.tracker(), context.iterationNumber(), isTracing())) {
                LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriedCouple.size());
                for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                    Integer marriageIndex = childrenForMarriage.getKey();
                    List<Integer> children = childrenForMarriage.getValue();
                    HashMap<MarriageAgent.SpouseType, String> marriage = marriedCouple.get(marriageIndex);

                    for (Integer childIndex : children) {
                        String childEmail = childrenEmails.get(childIndex);
                        insertParentship(tx, marriage, childEmail);
                    }
                }
                tx.commit();
            }
        }

        return reports;
    }

    protected abstract List<String> matchBirthsInCity(TX tx, GeoData.City region, LocalDateTime today);

    protected abstract List<HashMap<MarriageAgent.SpouseType, String>> matchMarriedCouple(TX tx, GeoData.City region);

    protected abstract void insertParentship(TX tx, HashMap<MarriageAgent.SpouseType, String> marriage, String childEmail);
}
