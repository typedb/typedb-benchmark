package grakn.simulation.test;

import grakn.simulation.db.common.action.Action;
import grakn.simulation.db.common.agent.base.Agent;
import grakn.simulation.db.grakn.driver.GraknDriver;
import grakn.simulation.db.neo4j.driver.Neo4jDriver;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static grakn.simulation.test.SimulationsForComparison.grakn;
import static grakn.simulation.test.SimulationsForComparison.neo4j;
import static org.junit.Assert.assertEquals;

@RunWith(ComparisonTestSuite.class)
public class ComparisonTest {

    private void compareReports(String agentName) {

        Agent<?, GraknDriver, ?>.Report graknAgentReport = grakn.getReport().getAgentReport(agentName);
        Agent<?, Neo4jDriver, ?>.Report neo4jAgentReport = neo4j.getReport().getAgentReport(agentName);

        if (!graknAgentReport.equals(neo4jAgentReport)) {
            graknAgentReport.trackers().forEach(tracker -> {
                Agent<?, GraknDriver, ?>.RegionalAgent.Report graknRegionReport = graknAgentReport.getRegionalAgentReport(tracker);
                Agent<?, Neo4jDriver, ?>.RegionalAgent.Report neo4jRegionReport = neo4jAgentReport.getRegionalAgentReport(tracker);

                if (!graknRegionReport.equals(neo4jRegionReport)) {
                    graknRegionReport.actionNames().forEach(actionName -> {
                        ArrayList<Action<?, ?>.Report> graknActionReport = graknRegionReport.getActionReport(actionName);
                        ArrayList<Action<?, ?>.Report> neo4jActionReport = neo4jRegionReport.getActionReport(actionName);
                        assertEquals(graknActionReport, neo4jActionReport);
                    });
                }
            });
        }
    }

    @Test
    public void testMarriageAgent() {
        compareReports("MarriageAgent");
    }

    @Test
    public void testPersonBirthAgent() {
        compareReports("PersonBirthAgent");
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
        compareReports("CompanyAgent");
    }

    @Test
    public void testEmploymentAgent() {
        compareReports("EmploymentAgent");
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
