package grakn.simulation.test;

import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.grakn.driver.GraknDriver;
import grakn.simulation.db.neo4j.driver.Neo4jDriver;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashMap;

import static grakn.simulation.test.SimulationsUnderTest.graknSimulation;
import static grakn.simulation.test.SimulationsUnderTest.neo4jSimulation;
import static org.junit.Assert.assertEquals;

@RunWith(SimulationTestSuite.class)
public class SimulationTest {

    private void compareFields(String agentName) {

        Agent<?, GraknDriver>.Report graknReport = graknSimulation.getReport().getAgentReport(agentName);
        Agent<?, Neo4jDriver>.Report neo4jReport = neo4jSimulation.getReport().getAgentReport(agentName);

        // Check both have the same trackers present
        assertEquals(graknReport.trackers(), neo4jReport.trackers());

        // Iterate over the trackers comparing the values
//        TODO bring in a count check, as below
//        graknResult.keySet().forEach(tracker -> assertEquals(graknResult.get(tracker), neo4jResult.get(tracker)));
        graknReport.trackers().forEach(tracker -> {
            Collection<HashMap<ComparableField, Object>> graknFields = graknReport.getRegionalAgentReport(tracker);
            Collection<HashMap<ComparableField, Object>> neo4jFields = neo4jReport.getRegionalAgentReport(tracker);
            assertEquals(graknFields, neo4jFields);
        });
    }

    @Test
    public void testMarriageAgent() {
        compareFields("MarriageAgent");
    }

    @Test
    public void testPersonBirthAgent() {
        compareFields("PersonBirthAgent");
    }

//    @Test
//    public void testAgeUpdateAgent() {
//    Comparing results of this agent will require sending a set equal to the size of the number of people in each city,
//    which doesn't scale well. So this is skipped in favour of spot-testing
//    }

    @Test
    public void testParentshipAgent() {
        compareFields("ParentshipAgent");
    }

//    @Test
//    public void testRelocationAgent() {
//        compareFields("RelocationAgent");
//    }

    @Test
    public void testCompanyAgent() {
        compareFields("CompanyAgent");
    }

    @Test
    public void testEmploymentAgent() {
        compareFields("EmploymentAgent");
    }

//    @Test
//    public void testProductAgent() {
//        compareFields("ProductAgent");
//    }
//
//    @Test
//    public void testTransactionAgent() {
//        compareFields("TransactionAgent");
//    }
//
//    @Test
//    public void testFriendshipAgent() {
//        compareFields("FriendshipAgent");
//    }
}
