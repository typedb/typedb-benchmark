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
import grakn.benchmark.simulation.agent.write.CompanyAgent;
import grakn.benchmark.simulation.agent.write.PersonBirthAgent;
import grakn.benchmark.simulation.agent.write.ProductAgent;
import grakn.benchmark.simulation.agent.write.PurchaseAgent;
import org.junit.Test;
import org.junit.runner.RunWith;

import static grakn.benchmark.test.BenchmarksForComparison.graknCore;
import static grakn.benchmark.test.BenchmarksForComparison.neo4j;
import static org.junit.Assert.assertEquals;

@RunWith(ComparisonTestSuite.class)
public class ComparisonTest {

    // TODO: raw usage of class
    private void compareReports(Class<? extends Agent> agentClass) {
        assertEquals(graknCore.getReport(agentClass), neo4j.getReport(agentClass));
    }

//    @Test
//    public void testMarriageAgent() {
//        compareReports("MarriageAgent");
//    }

    @Test
    public void testPersonBirthAgent() {
        compareReports(PersonBirthAgent.class);
    }

//    @Test
//    public void testAgeUpdateAgent() {
//    Comparing results of this agent will require sending a set equal to the size of the number of people in each city,
//    which doesn't scale well. So this is skipped in favour of spot-testing
//    }

//    @Test
//    public void testParentshipAgent() {
//        compareReports("ParentshipAgent");
//    }

//    @Test
//    public void testRelocationAgent() {
//        compareFields("RelocationAgent");
//    }

    @Test
    public void testCompanyAgent() {
        compareReports(CompanyAgent.class);
    }

//    @Test
//    public void testEmploymentAgent() {
//        compareReports("EmploymentAgent");
//    }

    @Test
    public void testProductAgent() {
        compareReports(ProductAgent.class);
    }

    @Test
    public void testPurchaseAgent() {
        compareReports(PurchaseAgent.class);
    }

//    @Test
//    public void testFriendshipAgent() {
//        compareFields("FriendshipAgent");
//    }
}
