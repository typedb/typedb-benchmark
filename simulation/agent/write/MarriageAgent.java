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
import grakn.benchmark.simulation.agent.base.AgentManager;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgentManager;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class MarriageAgent<TX extends Transaction> extends CityAgentManager<TX> {

    public MarriageAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(int iteration, String tracker, Random random, boolean test) {
        return new City(iteration, tracker, random, test);
    }

    public class City extends CityAgent {
        public City(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<TX> session, World.City city) {

            // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
            LocalDateTime dobOfAdults = benchmarkContext.today().minusYears(benchmarkContext.world().AGE_OF_ADULTHOOD);
            List<String> womenEmails;
            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                womenEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "female", dobOfAdults), isTest(), actionReports());
                shuffle(womenEmails, random());
            }

            List<String> menEmails;
            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                menEmails = runAction(actionFactory().unmarriedPeopleInCityAction(dbOperation, city, "male", dobOfAdults), isTest(), actionReports());
                shuffle(menEmails, random());
            }

            int numMarriagesPossible = Math.min(benchmarkContext.world().getScaleFactor(), Math.min(womenEmails.size(), menEmails.size()));
            if (iteration() >= 5) {
                System.out.println("asdf");
                assert true;
            }
            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {
                if (numMarriagesPossible > 0) {
                    for (int i = 0; i < numMarriagesPossible; i++) {
                        String wifeEmail = womenEmails.get(i);
                        String husbandEmail = menEmails.get(i);
                        int marriageIdentifier = uniqueId(benchmarkContext, tracker(), i).hashCode();
                        runAction((Action<?, ?>) actionFactory().insertMarriageAction(dbOperation, city, marriageIdentifier, wifeEmail, husbandEmail), isTest(), actionReports());
                    }
                    dbOperation.commit();
                }
            }
        }
    }
}
