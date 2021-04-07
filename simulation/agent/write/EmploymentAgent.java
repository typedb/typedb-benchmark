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
import grakn.benchmark.simulation.action.read.CompaniesInCountryAction;
import grakn.benchmark.simulation.action.read.ResidentsInCityAction;
import grakn.benchmark.simulation.agent.base.RandomValueGenerator;
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.region.CityAgentManager;
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Session;
import grakn.benchmark.simulation.driver.Transaction;
import grakn.benchmark.simulation.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static grakn.benchmark.simulation.agent.base.Allocation.allocate;

public class EmploymentAgent<TX extends Transaction> extends CityAgentManager<TX> {

    private static final double MIN_ANNUAL_WAGE = 18000.00;
    private static final double MAX_ANNUAL_WAGE = 80000.00;
    private static final double MIN_CONTRACTED_HOURS = 30.0;
    private static final double MAX_CONTRACTED_HOURS = 70.0;
    private static final int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    private static final int MAX_CONTRACT_CHARACTER_LENGTH = 600;

    public EmploymentAgent(Client<TX> dbDriver, ActionFactory<TX, ?> actionFactory, SimulationContext benchmarkContext) {
        super(dbDriver, actionFactory, benchmarkContext);
    }

    @Override
    protected Agent getAgent(World.City region, Random random, SimulationContext context) {
        return new City(region, random, context);
    }

    public class City extends CityAgent {

        public City(World.City region, Random random, SimulationContext context) {
            super(region, random, context);
        }

        @Override
        protected void run(Session<TX> session, World.City city) {
            LocalDateTime employmentDate = context.today().minusYears(0);
            List<String> employeeEmails;
            List<Long> companyNumbers;

            try (TX tx = session.newTransaction(tracker(), iteration(), isTracing())) {
                ResidentsInCityAction<TX> employeeEmailsAction = actionFactory().residentsInCityAction(tx, city, context.world().getScaleFactor(), employmentDate);
                employeeEmails = runAction(employeeEmailsAction, isTest(), actionReports());
            }

            try (TX tx = session.newTransaction(tracker(), iteration(), isTracing())) {
                CompaniesInCountryAction<TX> companyNumbersAction = actionFactory().companiesInCountryAction(tx, city.country(), context.world().getScaleFactor());
                companyNumbers = runAction(companyNumbersAction, isTest(), actionReports());
            }

            try (TX tx = session.newTransaction(tracker(), iteration(), isTracing())) {
                // A second transaction is being used to circumvent graknlabs/grakn issue #5585
                boolean allocated = allocate(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> {
                    double wageValue = RandomValueGenerator.of(random()).boundRandomDouble(MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
                    String contractContent = RandomValueGenerator.of(random()).boundRandomLengthRandomString(MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH);
                    double contractedHours = RandomValueGenerator.of(random()).boundRandomDouble(MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
                    runAction((Action<?, ?>) actionFactory().insertEmploymentAction(tx, city, employeeEmail, companyNumber, employmentDate, wageValue, contractContent, contractedHours), isTest(), actionReports());
                });
                if (allocated) {
                    tx.commit();
                }
            }
        }
    }
}
