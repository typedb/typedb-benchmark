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
import grakn.benchmark.simulation.agent.base.AgentManager;
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
import grakn.benchmark.simulation.driver.Client;
import grakn.benchmark.simulation.driver.Transaction;

public class AgentFactory<TX extends Transaction, ACTION_FACTORY extends ActionFactory<TX, ?>> {

    private final Client<TX> dbDriver;
    private final ACTION_FACTORY actionFactory;
    private final grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext;

    public AgentFactory(Client<TX> dbDriver, ACTION_FACTORY actionFactory, grakn.benchmark.simulation.agent.base.BenchmarkContext benchmarkContext) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        this.benchmarkContext = benchmarkContext;
    }

    public MarriageAgent<TX> marriage() {
        return new MarriageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public PersonBirthAgent<TX> personBirth() {
        return new PersonBirthAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public AgeUpdateAgent<TX> ageUpdate() {
        return new AgeUpdateAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ParentshipAgent<TX> parentship() {
        return new ParentshipAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public RelocationAgent<TX> relocation() {
        return new RelocationAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public CompanyAgent<TX> company() {
        return new CompanyAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public EmploymentAgent<TX> employment() {
        return new EmploymentAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ProductAgent<TX> product() {
        return new ProductAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public PurchaseAgent<TX> transaction() {
        return new PurchaseAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FriendshipAgent<TX> friendship() {
        return new FriendshipAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public MeanWageAgent<TX> meanWage() {
        return new MeanWageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindLivedInAgent<TX> findLivedIn() {
        return new FindLivedInAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindCurrentResidentsAgent<TX> findCurrentResidents() {
        return new FindCurrentResidentsAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindTransactionCurrencyAgent<TX> findTransactionCurrency() {
        return new FindTransactionCurrencyAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ArbitraryOneHopAgent<TX> arbitraryOneHop() {
        return new ArbitraryOneHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public TwoHopAgent<TX> twoHop() {
        return new TwoHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public ThreeHopAgent<TX> threeHop() {
        return new ThreeHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FourHopAgent<TX> fourHop() {
        return new FourHopAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindSpecificMarriageAgent<TX> findSpecificMarriage() {
        return new FindSpecificMarriageAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public FindSpecificPersonAgent<TX> findSpecificPerson() {
        return new FindSpecificPersonAgent<>(dbDriver, actionFactory, benchmarkContext);
    }

    public AgentManager<?, TX> get(String agentName) {
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
