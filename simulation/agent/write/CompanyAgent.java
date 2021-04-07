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
import grakn.benchmark.simulation.agent.region.CountryAgent;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Random;

public class CompanyAgent<TX extends Transaction> extends CountryAgent<TX> {

    public CompanyAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(World.Country region, Random random, SimulationContext context) {
        return new Country(region, random, context);
    }

    public class Country extends CountryRegion {
        public Country(World.Country region, Random random, SimulationContext context) {
            super(region, random, context);
        }

        @Override
        protected void run(Session<TX> session, World.Country region, List<Action<?, ?>.Report> reports, Random random) {
            int numCompanies = context.world().getScaleFactor();

            try (TX dbOperation = session.newTransaction(region.tracker(), context.iteration(), isTracing())) {

                for (int i = 0; i < numCompanies; i++) {
                    // TODO can be a util
                    String adjective = pickOne(context.world().getAdjectives(), random);
                    // TODO can be a util
                    String noun = pickOne(context.world().getNouns(), random);

                    int companyNumber = uniqueId(context, region.tracker(), i).hashCode();
                    String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
                    runAction((Action<?, ?>) actionFactory().insertCompanyAction(dbOperation, region, context.today(), companyNumber, companyName), context.isTest(), reports);
                }
                dbOperation.commit();
            }
        }
    }
}