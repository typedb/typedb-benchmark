package grakn.simulation.test;

import grakn.simulation.db.common.agents.base.Agent.ComparableField;
import grakn.simulation.db.common.agents.base.AgentResult;
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

        ConcurrentHashMap<String, AgentResult> graknResult = graknSimulation.getResultHandler().getResultForAgent(agentName);
        ConcurrentHashMap<String, AgentResult> neo4jResult = neo4jSimulation.getResultHandler().getResultForAgent(agentName);

        // Check both have the same trackers present
        assertEquals(graknResult.keySet(), neo4jResult.keySet());

        // Iterate over the trackers comparing the values
//        TODO bring in a count check, as below
//        graknResult.keySet().forEach(tracker -> assertEquals(graknResult.get(tracker), neo4jResult.get(tracker)));
        graknResult.keySet().forEach(tracker -> {
            Collection<HashMap<ComparableField, Object>> graknFields = graknResult.get(tracker).getAllFieldValues();
            Collection<HashMap<ComparableField, Object>> neo4jFields = neo4jResult.get(tracker).getAllFieldValues();
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

//    @Test
//    public void testAgeUpdateAgent() {
//
//    }
}
