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

package grakn.benchmark.simulation.agent;

import grakn.benchmark.simulation.action.ActionFactory;
import grakn.benchmark.simulation.agent.base.Agent;
import grakn.benchmark.simulation.agent.insight.ArbitraryOneHopAgent;
import grakn.benchmark.simulation.agent.insight.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.agent.insight.FindLivedInAgent;
import grakn.benchmark.simulation.agent.insight.FindSpecificMarriageAgent;
import grakn.benchmark.simulation.agent.insight.FindSpecificPersonAgent;
import grakn.benchmark.simulation.agent.insight.FindTransactionCurrencyAgent;
import grakn.benchmark.simulation.agent.insight.FourHopAgent;
import grakn.benchmark.simulation.agent.insight.MeanWageAgent;
import grakn.benchmark.simulation.agent.insight.ThreeHopAgent;
import grakn.benchmark.simulation.agent.insight.TwoHopAgent;
import grakn.benchmark.simulation.agent.write.AgeUpdateAgent;
import grakn.benchmark.simulation.agent.write.CompanyAgent;
import grakn.benchmark.simulation.agent.write.EmploymentAgent;
import grakn.benchmark.simulation.agent.write.FriendshipAgent;
import grakn.benchmark.simulation.agent.write.MarriageAgent;
import grakn.benchmark.simulation.agent.write.ParentshipAgent;
import grakn.benchmark.simulation.agent.write.PersonBirthAgent;
import grakn.benchmark.simulation.agent.write.ProductAgent;
import grakn.benchmark.simulation.agent.write.RelocationAgent;
import grakn.benchmark.simulation.agent.write.PurchaseAgent;
import grakn.benchmark.simulation.driver.DbDriver;
import grakn.benchmark.simulation.driver.DbOperation;

public class AgentFactory<DB_OPERATION extends DbOperation, ACTION_FACTORY extends ActionFactory<DB_OPERATION, ?>> {

    private final DbDriver<DB_OPERATION> dbDriver;
    private final ACTION_FACTORY actionFactory;
    private final grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext;

    public AgentFactory(DbDriver<DB_OPERATION> dbDriver, ACTION_FACTORY actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        this.benchmarkContext = benchmarkContext;
    }

    public MarriageAgent<DB_OPERATION> marriage() {
        return new MarriageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public PersonBirthAgent<DB_OPERATION> personBirth() {
        return new PersonBirthAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public AgeUpdateAgent<DB_OPERATION> ageUpdate() {
        return new AgeUpdateAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ParentshipAgent<DB_OPERATION> parentship() {
        return new ParentshipAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public RelocationAgent<DB_OPERATION> relocation() {
        return new RelocationAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public CompanyAgent<DB_OPERATION> company() {
        return new CompanyAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public EmploymentAgent<DB_OPERATION> employment() {
        return new EmploymentAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ProductAgent<DB_OPERATION> product() {
        return new ProductAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public PurchaseAgent<DB_OPERATION> transaction() {
        return new PurchaseAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FriendshipAgent<DB_OPERATION> friendship() {
        return new FriendshipAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public MeanWageAgent<DB_OPERATION> meanWage() {
        return new MeanWageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindLivedInAgent<DB_OPERATION> findLivedIn() {
        return new FindLivedInAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindCurrentResidentsAgent<DB_OPERATION> findCurrentResidents() {
        return new FindCurrentResidentsAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindTransactionCurrencyAgent<DB_OPERATION> findTransactionCurrency() {
        return new FindTransactionCurrencyAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ArbitraryOneHopAgent<DB_OPERATION> arbitraryOneHop() {
        return new ArbitraryOneHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public TwoHopAgent<DB_OPERATION> twoHop() {
        return new TwoHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ThreeHopAgent<DB_OPERATION> threeHop() {
        return new ThreeHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FourHopAgent<DB_OPERATION> fourHop() {
        return new FourHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindSpecificMarriageAgent<DB_OPERATION> findSpecificMarriage() {
        return new FindSpecificMarriageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindSpecificPersonAgent<DB_OPERATION> findSpecificPerson() {
        return new FindSpecificPersonAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public Agent<?, DB_OPERATION> get(String agentName) {
        switch (agentName) {
            case "marriage":
                return marriage();
            case "personBirth":
                return personBirth();
            case "ageUpdate":
                return ageUpdate();
            case "parentship":
                return parentship();
            case "relocation":
                return relocation();
            case "company":
                return company();
            case "employment":
                return employment();
            case "product":
                return product();
            case "transaction":
                return transaction();
            case "friendship":
                return friendship();
            case "meanWage":
                return meanWage();
            case "findLivedIn":
                return findLivedIn();
            case "findCurrentResidents":
                return findCurrentResidents();
            case "findTransactionCurrency":
                return findTransactionCurrency();
            case "arbitraryOneHop":
                return arbitraryOneHop();
            case "twoHop":
                return twoHop();
            case "threeHop":
                return threeHop();
            case "fourHop":
                return fourHop();
            case "findSpecificMarriage":
                return findSpecificMarriage();
            case "findSpecificPerson":
                return findSpecificPerson();
            default:
                throw new IllegalArgumentException("Unrecognised agent name: " + agentName);
        }
    }
}
