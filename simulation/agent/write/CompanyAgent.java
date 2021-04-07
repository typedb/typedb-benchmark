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
import grakn.benchmark.simulation.agent.region.CountryAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class CompanyAgent<TX extends Transaction> extends CountryAgent<TX> {

    public CompanyAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(int iteration, String tracker, Random random, boolean test) {
        return new Country(iteration, tracker, random, test);
    }

    public class Country extends CountryRegion {
        public Country(int iteration, String tracker, Random random, boolean test) {
            super(iteration, tracker, random, test);
        }

        @Override
        protected void run(Session<TX> session, World.Country country) {
            int numCompanies = benchmarkContext.world().getScaleFactor();

            try (TX dbOperation = session.newTransaction(tracker(), iteration(), isTracing())) {

                for (int i = 0; i < numCompanies; i++) {
                    // TODO can be a util
                    String adjective = pickOne(benchmarkContext.world().getAdjectives(), random());
                    // TODO can be a util
                    String noun = pickOne(benchmarkContext.world().getNouns(), random());

                    int companyNumber = uniqueId(benchmarkContext, tracker(), i).hashCode();
                    String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                    runAction((Action<?, ?>) actionFactory().insertCompanyAction(dbOperation, country, benchmarkContext.today(), companyNumber, companyName), isTest(), actionReports());
                }
                dbOperation.commit();
            }
        }
    }
}