package grakn.simulation.test;

import grakn.simulation.db.common.agents.base.Agent.ComparableField;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static grakn.simulation.test.SimulationsUnderTest.graknSimulation;
import static grakn.simulation.test.SimulationsUnderTest.neo4jSimulation;
import static org.junit.Assert.assertEquals;

@RunWith(SimulationTestSuite.class)
public class SimulationTest {

    private void compareFields(String agentName) {

        ConcurrentHashMap<String, AgentResultSet> graknResult = graknSimulation.getResultHandler().getResultForAgent(agentName);
        ConcurrentHashMap<String, AgentResultSet> neo4jResult = neo4jSimulation.getResultHandler().getResultForAgent(agentName);

        // Check both have the same trackers present
        assertEquals(graknResult.keySet(), neo4jResult.keySet());

        // Iterate over the trackers comparing the values
//        TODO bring in a count check, as below
//        graknResult.keySet().forEach(tracker -> assertEquals(graknResult.get(tracker), neo4jResult.get(tracker)));
        graknResult.keySet().forEach(tracker -> {
            Collection<HashMap<ComparableField, Object>> graknFields = graknResult.get(tracker);
            Collection<HashMap<ComparableField, Object>> neo4jFields = neo4jResult.get(tracker);
            assertEquals(graknFields, neo4jFields);
        });
    }

    @Test
    public void testPersonBirthAgent() {
        compareFields("PersonBirthAgent");
    }

    @Test
    public void testMarriageAgent() {
        compareFields("MarriageAgent");
    }

    @Test
    public void testCompanyAgent() {
        compareFields("CompanyAgent");
    }

    @Test
    public void testEmploymentAgent() {
        compareFields("EmploymentAgent");
    }

//    @Test
//    public void testAgeUpdateAgent() {
//    Comparing results of this agent will require sending a set equal to the size of the number of people in each city,
//    which doesn't scale well. So this is skipped in favour of spot-testing
//    }
}
