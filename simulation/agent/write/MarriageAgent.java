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

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.region.CityAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class MarriageAgent<DB_OPERATION extends Transaction> extends CityAgent<DB_OPERATION> {

    public MarriageAgent(Client<DB_OPERATION> dbDriver, ActionFactory<DB_OPERATION, ?> actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Regional getRegionalAgent(int iteration, String tracker, Random random, boolean test) {
        return new City(iteration, tracker, random, test);
    }

    public class City extends CityRegion {
        public City(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<DB_OPERATION> dbOperationFactory, World.City city) {

            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            LocalDateTime dobOfAdults = benchmarkContext.today().minusYears(benchmarkContext.world().AGE_OF_ADULTHOOD);
            List<String> womenEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                womenEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "female", dobOfAdults));
                shuffle(womenEmails);
            }

            List<String> menEmails;
            try (DB_OPERATION dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                menEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "male", dobOfAdults));
                shuffle(menEmails);
            }

            int numMarriagesPossible = Math.min(benchmarkContext.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
            if (iteration() >= 5) {
                System.out.println("asdf");
                assert true;
            }
            try (DB_OPERATION dbOperation = dbOperationFactory.newTransaction(tracker(), iteration(), isTracing())) {
                if (numMarriagesPossible > 0) {
                    for (int i = 0; i < numMarriagesPossible; i++) {
                        String wifeEmail = womenEmails.get(i);
                        String husbandEmail = menEmails.get(i);
                        int marriageIdentifier = uniqueId(benchmarkContext, i).hashCode();
                        runAction(actionFactory().insertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail));
                    }
                    dbOperation.save();
                }
            }
        }
    }
}
