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

package grakn.benchmark.test;

import grakn.benchmark.simulation.agent.Agent;
import grakn.benchmark.simulation.agent.CompanyAgent;
import grakn.benchmark.simulation.agent.EmploymentAgent;
import grakn.benchmark.simulation.agent.FriendshipAgent;
import grakn.benchmark.simulation.agent.MarriageAgent;
import grakn.benchmark.simulation.agent.ParentshipAgent;
import grakn.benchmark.simulation.agent.PersonBirthAgent;
import grakn.benchmark.simulation.agent.ProductAgent;
import grakn.benchmark.simulation.agent.PurchaseAgent;
import grakn.benchmark.simulation.agent.RelocationAgent;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static grakn.benchmark.test.ComparisonTestSuite.GRAKN_CORE;
import static grakn.benchmark.test.ComparisonTestSuite.NEO4J;
import static org.junit.Assert.assertEquals;

@RunWith(ComparisonTestSuite.class)
public class ComparisonTest {

    // TODO: raw usage of class
    private void compareReports(Class<? extends Agent> agentClass) {
        assertEquals(GRAKN_CORE.getReport(agentClass), NEO4J.getReport(agentClass));
    }

    // TODO: all these test methods can be replaced with Simulation.agentBuilders.keySet()
    @Test
    @Ignore
    public void testMarriageAgent() {
        compareReports(MarriageAgent.class);
    }

    @Test
    public void testPersonBirthAgent() {
        compareReports(PersonBirthAgent.class);
    }

    @Test
    @Ignore
    public void testAgeUpdateAgent() {
//    Comparing results of this agent will require sending a set equal to the size of the number of people in each city,
//    which doesn't scale well. So this is skipped in favour of spot-testing
    }

    @Test
    public void testParentshipAgent() {
        compareReports(ParentshipAgent.class);
    }

    @Test
    public void testRelocationAgent() {
        compareReports(RelocationAgent.class);
    }

    @Test
    public void testCompanyAgent() {
        compareReports(CompanyAgent.class);
    }

    @Test
    @Ignore
    public void testEmploymentAgent() {
        compareReports(EmploymentAgent.class);
    }

    @Test
    public void testProductAgent() {
        compareReports(ProductAgent.class);
    }

    @Test
    public void testPurchaseAgent() {
        compareReports(PurchaseAgent.class);
    }

    @Test
    @Ignore
    public void testFriendshipAgent() {
        compareReports(FriendshipAgent.class);
    }
}
