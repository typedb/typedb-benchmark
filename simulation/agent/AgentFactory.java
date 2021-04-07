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
import grakn.benchmark.simulation.agent.base.SimulationContext;
import grakn.benchmark.simulation.agent.read.ArbitraryOneHopAgent;
import grakn.benchmark.simulation.agent.read.FindCurrentResidentsAgent;
import grakn.benchmark.simulation.agent.read.FindLivedInAgent;
import grakn.benchmark.simulation.agent.read.FindSpecificMarriageAgent;
import grakn.benchmark.simulation.agent.read.FindSpecificPersonAgent;
import grakn.benchmark.simulation.agent.read.FindTransactionCurrencyAgent;
import grakn.benchmark.simulation.agent.read.FourHopAgent;
import grakn.benchmark.simulation.agent.read.MeanWageAgent;
import grakn.benchmark.simulation.agent.read.ThreeHopAgent;
import grakn.benchmark.simulation.agent.read.TwoHopAgent;
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

    private final Client<TX> client;
    private final ACTION_FACTORY actionFactory;
    private final SimulationContext benchmarkContext;

    public AgentFactory(Client<TX> client, ACTION_FACTORY actionFactory, SimulationContext benchmarkContext) {
        this.client = client;
        this.actionFactory = actionFactory;
        this.benchmarkContext = benchmarkContext;
    }

    public MarriageAgent<TX> marriage() {
        return new MarriageAgent<>(client, actionFactory, benchmarkContext);
    }

    public PersonBirthAgent<TX> personBirth() {
        return new PersonBirthAgent<>(client, actionFactory, benchmarkContext);
    }

    public AgeUpdateAgent<TX> ageUpdate() {
        return new AgeUpdateAgent<>(client, actionFactory, benchmarkContext);
    }

    public ParentshipAgent<TX> parentship() {
        return new ParentshipAgent<>(client, actionFactory, benchmarkContext);
    }

    public RelocationAgent<TX> relocation() {
        return new RelocationAgent<>(client, actionFactory, benchmarkContext);
    }

    public CompanyAgent<TX> company() {
        return new CompanyAgent<>(client, actionFactory, benchmarkContext);
    }

    public EmploymentAgent<TX> employment() {
        return new EmploymentAgent<>(client, actionFactory, benchmarkContext);
    }

    public ProductAgent<TX> product() {
        return new ProductAgent<>(client, actionFactory, benchmarkContext);
    }

    public PurchaseAgent<TX> transaction() {
        return new PurchaseAgent<>(client, actionFactory, benchmarkContext);
    }

    public FriendshipAgent<TX> friendship() {
        return new FriendshipAgent<>(client, actionFactory, benchmarkContext);
    }

    public MeanWageAgent<TX> meanWage() {
        return new MeanWageAgent<>(client, actionFactory, benchmarkContext);
    }

    public FindLivedInAgent<TX> findLivedIn() {
        return new FindLivedInAgent<>(client, actionFactory, benchmarkContext);
    }

    public FindCurrentResidentsAgent<TX> findCurrentResidents() {
        return new FindCurrentResidentsAgent<>(client, actionFactory, benchmarkContext);
    }

    public FindTransactionCurrencyAgent<TX> findTransactionCurrency() {
        return new FindTransactionCurrencyAgent<>(client, actionFactory, benchmarkContext);
    }

    public ArbitraryOneHopAgent<TX> arbitraryOneHop() {
        return new ArbitraryOneHopAgent<>(client, actionFactory, benchmarkContext);
    }

    public TwoHopAgent<TX> twoHop() {
        return new TwoHopAgent<>(client, actionFactory, benchmarkContext);
    }

    public ThreeHopAgent<TX> threeHop() {
        return new ThreeHopAgent<>(client, actionFactory, benchmarkContext);
    }

    public FourHopAgent<TX> fourHop() {
        return new FourHopAgent<>(client, actionFactory, benchmarkContext);
    }

    public FindSpecificMarriageAgent<TX> findSpecificMarriage() {
        return new FindSpecificMarriageAgent<>(client, actionFactory, benchmarkContext);
    }

    public FindSpecificPersonAgent<TX> findSpecificPerson() {
        return new FindSpecificPersonAgent<>(client, actionFactory, benchmarkContext);
    }

    public Agent<?, TX> get(String agentName) {
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
