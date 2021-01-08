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

package grakn.benchmark.common.agent;

import grakn.benchmark.common.action.ActionFactory;
import grakn.benchmark.common.agent.base.Agent;
import grakn.benchmark.common.agent.base.SimulationContext;
import grakn.benchmark.common.agent.insight.ArbitraryOneHopAgent;
import grakn.benchmark.common.agent.insight.FindCurrentResidentsAgent;
import grakn.benchmark.common.agent.insight.FindLivedInAgent;
import grakn.benchmark.common.agent.insight.FindSpecificMarriageAgent;
import grakn.benchmark.common.agent.insight.FindSpecificPersonAgent;
import grakn.benchmark.common.agent.insight.FindTransactionCurrencyAgent;
import grakn.benchmark.common.agent.insight.FourHopAgent;
import grakn.benchmark.common.agent.insight.MeanWageAgent;
import grakn.benchmark.common.agent.insight.ThreeHopAgent;
import grakn.benchmark.common.agent.insight.TwoHopAgent;
import grakn.benchmark.common.agent.write.AgeUpdateAgent;
import grakn.benchmark.common.agent.write.CompanyAgent;
import grakn.benchmark.common.agent.write.EmploymentAgent;
import grakn.benchmark.common.agent.write.FriendshipAgent;
import grakn.benchmark.common.agent.write.MarriageAgent;
import grakn.benchmark.common.agent.write.ParentshipAgent;
import grakn.benchmark.common.agent.write.PersonBirthAgent;
import grakn.benchmark.common.agent.write.ProductAgent;
import grakn.benchmark.common.agent.write.RelocationAgent;
import grakn.benchmark.common.agent.write.TransactionAgent;
import grakn.benchmark.common.driver.DbDriver;
import grakn.benchmark.common.driver.DbOperation;

public class AgentFactory<DB_OPERATION extends DbOperation, ACTION_FACTORY extends ActionFactory<DB_OPERATION, ?>> {

    private final DbDriver<DB_OPERATION> dbDriver;
    private final ACTION_FACTORY actionFactory;
    private final SimulationContext simulationContext;

    public AgentFactory(DbDriver<DB_OPERATION> dbDriver, ACTION_FACTORY actionFactory, SimulationContext simulationContext) {
        this.dbDriver = dbDriver;
        this.actionFactory = actionFactory;
        this.simulationContext = simulationContext;
    }

    public MarriageAgent<DB_OPERATION> marriage() {
        return new MarriageAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public PersonBirthAgent<DB_OPERATION> personBirth() {
        return new PersonBirthAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public AgeUpdateAgent<DB_OPERATION> ageUpdate() {
        return new AgeUpdateAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public ParentshipAgent<DB_OPERATION> parentship() {
        return new ParentshipAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public RelocationAgent<DB_OPERATION> relocation() {
        return new RelocationAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public CompanyAgent<DB_OPERATION> company() {
        return new CompanyAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public EmploymentAgent<DB_OPERATION> employment() {
        return new EmploymentAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public ProductAgent<DB_OPERATION> product() {
        return new ProductAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public TransactionAgent<DB_OPERATION> transaction() {
        return new TransactionAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FriendshipAgent<DB_OPERATION> friendship() {
        return new FriendshipAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public MeanWageAgent<DB_OPERATION> meanWage() {
        return new MeanWageAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FindLivedInAgent<DB_OPERATION> findLivedIn() {
        return new FindLivedInAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FindCurrentResidentsAgent<DB_OPERATION> findCurrentResidents() {
        return new FindCurrentResidentsAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FindTransactionCurrencyAgent<DB_OPERATION> findTransactionCurrency() {
        return new FindTransactionCurrencyAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public ArbitraryOneHopAgent<DB_OPERATION> arbitraryOneHop() {
        return new ArbitraryOneHopAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public TwoHopAgent<DB_OPERATION> twoHop() {
        return new TwoHopAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public ThreeHopAgent<DB_OPERATION> threeHop() {
        return new ThreeHopAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FourHopAgent<DB_OPERATION> fourHop() {
        return new FourHopAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FindSpecificMarriageAgent<DB_OPERATION> findSpecificMarriage() {
        return new FindSpecificMarriageAgent<>(dbDriver, actionFactory, simulationContext);
    }

    public FindSpecificPersonAgent<DB_OPERATION> findSpecificPerson() {
        return new FindSpecificPersonAgent<>(dbDriver, actionFactory, simulationContext);
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
